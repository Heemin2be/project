package seasonal.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seasonal.domain.MapMarker;
import seasonal.domain.MapViewModel;
import seasonal.enums.PlantType;
import seasonal.service.ObservationMapService;
import seasonal.web.dto.MapViewResponse;
import seasonal.web.dto.MarkerResponse;
import seasonal.web.dto.PlantResponse;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
public class SeasonalController {

    private final ObservationMapService mapService;

    public SeasonalController(ObservationMapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/api/plants")
    public List<PlantResponse> getPlants() {
        return Arrays.stream(PlantType.values())
                .map(p -> new PlantResponse(p.name(), p.getDisplayName()))
                .toList();
    }

    @GetMapping("/api/map")
    public MapViewResponse getMap(
            @RequestParam(defaultValue = "CHERRY_BLOSSOM") String plantType,
            @RequestParam(defaultValue = "") String date
    ) {
        PlantType type = PlantType.valueOf(plantType);
        LocalDate queryDate = date.isBlank() ? LocalDate.now() : LocalDate.parse(date);
        MapViewModel viewModel = mapService.createMapView(type, queryDate);
        return toResponse(viewModel);
    }

    // ── 변환 ──────────────────────────────────────────────────────

    private static MapViewResponse toResponse(MapViewModel model) {
        List<MarkerResponse> markers = model.getMarkers().stream()
                .map(SeasonalController::toMarkerResponse)
                .toList();
        return new MapViewResponse(
                model.getPlantType().name(),
                model.getPlantType().getDisplayName(),
                model.getQueryDate().toString(),
                markers
        );
    }

    private static MarkerResponse toMarkerResponse(MapMarker marker) {
        return new MarkerResponse(
                marker.getStation().getStationCode(),
                marker.getStation().getName(),
                marker.getStation().getAddress(),
                marker.getStation().getLocation().getLatitude(),
                marker.getStation().getLocation().getLongitude(),
                marker.getStage().name(),
                marker.getStage().getDisplayName(),
                marker.getColor(),
                marker.getComparison().name(),
                marker.getComparison().getDisplayName(),
                marker.getSourceType().name(),
                marker.getSourceType().getDisplayName(),
                marker.getObservedDate().toString()
        );
    }
}
