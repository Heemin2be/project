package seasonal.domain;

import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.Objects;

public final class NormalYearReference {
    private final String stationCode;
    private final PlantType plantType;
    private final LocalDate normalDate;

    public NormalYearReference(String stationCode, PlantType plantType, LocalDate normalDate) {
        if (stationCode == null || stationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("관측지점 코드는 비어 있을 수 없습니다.");
        }
        this.stationCode = stationCode.trim();
        this.plantType = Objects.requireNonNull(plantType, "식물 종류는 null일 수 없습니다.");
        this.normalDate = Objects.requireNonNull(normalDate, "평년 기준일은 null일 수 없습니다.");
    }

    public String getStationCode() {
        return stationCode;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public LocalDate getNormalDate() {
        return normalDate;
    }
}
