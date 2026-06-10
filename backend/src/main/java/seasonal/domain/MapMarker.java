package seasonal.domain;

import seasonal.enums.ComparisonResultType;
import seasonal.enums.DataSourceType;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.Objects;

public final class MapMarker {
    private final ObservationStation station;
    private final PlantType plantType;
    private final PhenologyStage stage;
    private final ComparisonResultType comparison;
    private final DataSourceType sourceType;
    private final LocalDate observedDate;
    private final String color;
    /** 현재 기온(℃). KMA API 조회 실패 시 null. */
    private final Double temperature;

    public MapMarker(
            ObservationStation station,
            PlantType plantType,
            PhenologyStage stage,
            ComparisonResultType comparison,
            DataSourceType sourceType,
            LocalDate observedDate,
            String color
    ) {
        this(station, plantType, stage, comparison, sourceType, observedDate, color, null);
    }

    public MapMarker(
            ObservationStation station,
            PlantType plantType,
            PhenologyStage stage,
            ComparisonResultType comparison,
            DataSourceType sourceType,
            LocalDate observedDate,
            String color,
            Double temperature
    ) {
        this.station = Objects.requireNonNull(station, "관측지점은 null일 수 없습니다.");
        this.plantType = Objects.requireNonNull(plantType, "식물 종류는 null일 수 없습니다.");
        this.stage = Objects.requireNonNull(stage, "관측 단계는 null일 수 없습니다.");
        this.comparison = Objects.requireNonNull(comparison, "비교 결과는 null일 수 없습니다.");
        this.sourceType = Objects.requireNonNull(sourceType, "데이터 출처는 null일 수 없습니다.");
        this.observedDate = Objects.requireNonNull(observedDate, "관측 날짜는 null일 수 없습니다.");
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("마커 색상은 비어 있을 수 없습니다.");
        }
        this.color = color;
        this.temperature = temperature;
    }

    public ObservationStation getStation() {
        return station;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public PhenologyStage getStage() {
        return stage;
    }

    public ComparisonResultType getComparison() {
        return comparison;
    }

    public DataSourceType getSourceType() {
        return sourceType;
    }

    public LocalDate getObservedDate() {
        return observedDate;
    }

    public String getColor() {
        return color;
    }

    /** KMA 초단기실황 기온(℃). API 키 미설정 또는 조회 실패 시 null. */
    public Double getTemperature() {
        return temperature;
    }
}
