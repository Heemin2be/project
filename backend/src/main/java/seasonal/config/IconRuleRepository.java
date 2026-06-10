package seasonal.config;

import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

public interface IconRuleRepository {
    String findColor(PlantType plantType, PhenologyStage stage);
}
