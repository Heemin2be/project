package seasonal.domain;

import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class MapViewModel {
    private final PlantType plantType;
    private final LocalDate queryDate;
    private final List<MapMarker> markers;

    public MapViewModel(PlantType plantType, LocalDate queryDate, List<MapMarker> markers) {
        this.plantType = Objects.requireNonNull(plantType, "식물 종류는 null일 수 없습니다.");
        this.queryDate = Objects.requireNonNull(queryDate, "조회 날짜는 null일 수 없습니다.");
        this.markers = List.copyOf(Objects.requireNonNull(markers, "마커 목록은 null일 수 없습니다."));
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public LocalDate getQueryDate() {
        return queryDate;
    }

    public List<MapMarker> getMarkers() {
        return markers;
    }
}
