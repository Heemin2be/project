package seasonal.domain;

import seasonal.enums.DataSourceType;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.Objects;

public final class ObservationRecord {
    private final String stationCode;
    private final PlantType plantType;
    private final LocalDate observedDate;
    private final PhenologyStage stage;
    private final DataSourceType sourceType;

    public ObservationRecord(
            String stationCode,
            PlantType plantType,
            LocalDate observedDate,
            PhenologyStage stage,
            DataSourceType sourceType
    ) {
        if (stationCode == null || stationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("관측지점 코드는 비어 있을 수 없습니다.");
        }
        this.stationCode = stationCode.trim();
        this.plantType = Objects.requireNonNull(plantType, "식물 종류는 null일 수 없습니다.");
        this.observedDate = Objects.requireNonNull(observedDate, "관측 날짜는 null일 수 없습니다.");
        this.stage = Objects.requireNonNull(stage, "관측 단계는 null일 수 없습니다.");
        this.sourceType = Objects.requireNonNull(sourceType, "데이터 출처는 null일 수 없습니다.");
    }

    public String getStationCode() {
        return stationCode;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public LocalDate getObservedDate() {
        return observedDate;
    }

    public PhenologyStage getStage() {
        return stage;
    }

    public DataSourceType getSourceType() {
        return sourceType;
    }
}
