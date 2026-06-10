package seasonal.repository;

import org.junit.jupiter.api.Test;
import seasonal.enums.PlantType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CsvRepositoryTest {
    @Test
    void loadsStationsAndObservations() {
        CsvStationRepository stationRepository = new CsvStationRepository();
        CsvObservationRepository observationRepository = new CsvObservationRepository();
        CsvNormalYearRepository normalYearRepository = new CsvNormalYearRepository();

        assertEquals(7, stationRepository.findAll().size());
        assertFalse(observationRepository.findByPlantType(PlantType.CHERRY_BLOSSOM).isEmpty());
        assertFalse(normalYearRepository.findByPlantType(PlantType.CHERRY_BLOSSOM).isEmpty());
    }
}
