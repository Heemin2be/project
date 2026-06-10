package seasonal.repository;

import seasonal.domain.ObservationRecord;
import seasonal.enums.DataSourceType;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.List;

public class CsvObservationRepository implements ObservationRepository {
    @Override
    public List<ObservationRecord> findByPlantType(PlantType plantType) {
        return CsvReader.readRows("/data/observations.csv").stream()
                .map(row -> new ObservationRecord(
                        row[0],
                        PlantType.valueOf(row[1]),
                        LocalDate.parse(row[2]),
                        PhenologyStage.valueOf(row[3]),
                        DataSourceType.valueOf(row[4])
                ))
                .filter(record -> record.getPlantType() == plantType)
                .toList();
    }
}
