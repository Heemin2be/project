package seasonal.repository;

import seasonal.domain.ObservationRecord;
import seasonal.enums.PlantType;

import java.util.List;

public interface ObservationRepository {
    List<ObservationRecord> findByPlantType(PlantType plantType);
}
