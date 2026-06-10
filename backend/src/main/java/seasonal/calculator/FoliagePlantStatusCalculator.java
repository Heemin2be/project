package seasonal.calculator;

import org.springframework.stereotype.Component;
import seasonal.domain.ObservationRecord;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

import java.time.LocalDate;

@Component
public class FoliagePlantStatusCalculator extends AbstractPhenologyStatusCalculator {
    @Override
    public boolean supports(PlantType plantType) {
        return plantType == PlantType.MAPLE;
    }

    @Override
    protected PhenologyStage calculateObservedStage(LocalDate queryDate, ObservationRecord record) {
        return record.getStage() == PhenologyStage.FULL_BLOOM
                ? PhenologyStage.PEAK
                : record.getStage();
    }
}
