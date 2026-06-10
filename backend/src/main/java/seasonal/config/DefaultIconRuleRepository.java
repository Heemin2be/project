package seasonal.config;

import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

public class DefaultIconRuleRepository implements IconRuleRepository {
    @Override
    public String findColor(PlantType plantType, PhenologyStage stage) {
        return stage.getColor();
    }
}
