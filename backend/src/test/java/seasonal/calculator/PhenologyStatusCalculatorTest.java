package seasonal.calculator;

import org.junit.jupiter.api.Test;
import seasonal.domain.ObservationRecord;
import seasonal.enums.DataSourceType;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PhenologyStatusCalculatorTest {
    private final StatusCalculatorResolver resolver = new StatusCalculatorResolver(List.of(
            new FloweringPlantStatusCalculator(),
            new FoliagePlantStatusCalculator()
    ));

    @Test
    void resolvesCalculatorByPlantType() {
        assertInstanceOf(FloweringPlantStatusCalculator.class, resolver.resolve(PlantType.CHERRY_BLOSSOM));
        assertInstanceOf(FoliagePlantStatusCalculator.class, resolver.resolve(PlantType.MAPLE));
    }

    @Test
    void returnsBeforeStartForEarlierQueryDate() {
        ObservationRecord record = new ObservationRecord(
                "SEOUL",
                PlantType.CHERRY_BLOSSOM,
                LocalDate.of(2026, 4, 3),
                PhenologyStage.STARTED,
                DataSourceType.CSV
        );

        PhenologyStage stage = resolver.resolve(record.getPlantType())
                .calculate(LocalDate.of(2026, 4, 1), record);

        assertEquals(PhenologyStage.BEFORE_START, stage);
    }
}
