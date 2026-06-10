package seasonal.repository;

import seasonal.domain.NormalYearReference;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.List;

public class CsvNormalYearRepository implements NormalYearRepository {
    @Override
    public List<NormalYearReference> findByPlantType(PlantType plantType) {
        return CsvReader.readRows("/data/normal_years.csv").stream()
                .map(row -> new NormalYearReference(row[0], PlantType.valueOf(row[1]), LocalDate.parse(row[2])))
                .filter(reference -> reference.getPlantType() == plantType)
                .toList();
    }
}
