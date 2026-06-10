package seasonal.config;

import org.springframework.stereotype.Component;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

@Component
public class DefaultIconRuleRepository implements IconRuleRepository {
    @Override
    public String findColor(PlantType plantType, PhenologyStage stage) {
        return stage.getColor();
    }
}
