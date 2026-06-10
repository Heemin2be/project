package seasonal.calculator;

import seasonal.enums.PlantType;

import java.util.List;

public class StatusCalculatorResolver {
    private final List<PhenologyStatusCalculator> calculators;

    public StatusCalculatorResolver(List<PhenologyStatusCalculator> calculators) {
        this.calculators = List.copyOf(calculators);
    }

    public PhenologyStatusCalculator resolve(PlantType plantType) {
        return calculators.stream()
                .filter(calculator -> calculator.supports(plantType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 식물 종류입니다: " + plantType));
    }
}
