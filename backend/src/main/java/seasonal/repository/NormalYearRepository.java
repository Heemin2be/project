package seasonal.repository;

import seasonal.domain.NormalYearReference;
import seasonal.enums.PlantType;

import java.util.List;

public interface NormalYearRepository {
    List<NormalYearReference> findByPlantType(PlantType plantType);
}
