package seasonal.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import seasonal.domain.GeoPoint;
import seasonal.domain.MapMarker;
import seasonal.domain.MapViewModel;
import seasonal.domain.ObservationStation;
import seasonal.enums.ComparisonResultType;
import seasonal.enums.DataSourceType;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;
import seasonal.service.ObservationMapService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeasonalController.class)
class SeasonalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObservationMapService mapService;

    @Test
    void returnsPlantsJson() throws Exception {
        mockMvc.perform(get("/api/plants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MAEHWA"))
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void returnsMapJson() throws Exception {
        ObservationStation station = new ObservationStation(
                "SEOUL", "서울", new GeoPoint(37.5, 126.9), "서울특별시");
        MapMarker marker = new MapMarker(
                station,
                PlantType.CHERRY_BLOSSOM,
                PhenologyStage.FULL_BLOOM,
                ComparisonResultType.NORMAL,
                DataSourceType.API,
                LocalDate.of(2026, 4, 1),
                "#F472B6"
        );
        MapViewModel viewModel = new MapViewModel(
                PlantType.CHERRY_BLOSSOM,
                LocalDate.of(2026, 4, 6),
                List.of(marker)
        );
        when(mapService.createMapView(any(), any())).thenReturn(viewModel);

        mockMvc.perform(get("/api/map?plantType=CHERRY_BLOSSOM&date=2026-04-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.markers[0].stationName").value("서울"))
                .andExpect(jsonPath("$.markers").isArray());
    }

    @Test
    void returnsBadRequestForInvalidPlantType() throws Exception {
        mockMvc.perform(get("/api/map?plantType=INVALID&date=2026-04-06"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void returnsEmptyMarkersWhenServiceReturnsNone() throws Exception {
        MapViewModel emptyViewModel = new MapViewModel(
                PlantType.CHERRY_BLOSSOM,
                LocalDate.of(2025, 1, 1),
                List.of()
        );
        when(mapService.createMapView(any(), any())).thenReturn(emptyViewModel);

        mockMvc.perform(get("/api/map?plantType=CHERRY_BLOSSOM&date=2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.markers").isEmpty());
    }
}
