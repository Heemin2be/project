package seasonal.calculator;

import seasonal.domain.ObservationRecord;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

import java.time.LocalDate;

public interface PhenologyStatusCalculator {
    boolean supports(PlantType plantType);

    PhenologyStage calculate(LocalDate queryDate, ObservationRecord record);
}
