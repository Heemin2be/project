package seasonal.domain;

import seasonal.enums.PlantType;
import seasonal.enums.SeasonType;

import java.util.Objects;

public final class SeasonalPlant {
    private final PlantType plantType;
    private final SeasonType seasonType;

    public SeasonalPlant(PlantType plantType, SeasonType seasonType) {
        this.plantType = Objects.requireNonNull(plantType, "식물 종류는 null일 수 없습니다.");
        this.seasonType = Objects.requireNonNull(seasonType, "계절은 null일 수 없습니다.");
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public SeasonType getSeasonType() {
        return seasonType;
    }
}
