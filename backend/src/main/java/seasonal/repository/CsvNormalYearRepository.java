package seasonal.repository;

import org.springframework.stereotype.Repository;
import seasonal.domain.NormalYearReference;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CsvNormalYearRepository implements NormalYearRepository {

    private static final List<NormalYearReference> ALL = List.of(
            // 벚꽃
            new NormalYearReference("JEJU",      PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 23)),
            new NormalYearReference("BUSAN",     PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 28)),
            new NormalYearReference("DAEGU",     PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 29)),
            new NormalYearReference("GWANGJU",   PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 29)),
            new NormalYearReference("DAEJEON",   PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  1)),
            new NormalYearReference("SEOUL",     PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  6)),
            new NormalYearReference("GANGNEUNG", PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  8)),
            // 매화
            new NormalYearReference("JEJU",      PlantType.MAEHWA, LocalDate.of(2026,  2, 14)),
            new NormalYearReference("BUSAN",     PlantType.MAEHWA, LocalDate.of(2026,  2, 21)),
            new NormalYearReference("DAEGU",     PlantType.MAEHWA, LocalDate.of(2026,  2, 26)),
            new NormalYearReference("SEOUL",     PlantType.MAEHWA, LocalDate.of(2026,  3,  6)),
            new NormalYearReference("GANGNEUNG", PlantType.MAEHWA, LocalDate.of(2026,  3, 14)),
            // 코스모스
            new NormalYearReference("BUSAN",     PlantType.COSMOS, LocalDate.of(2026,  8, 24)),
            new NormalYearReference("DAEGU",     PlantType.COSMOS, LocalDate.of(2026,  8, 30)),
            new NormalYearReference("SEOUL",     PlantType.COSMOS, LocalDate.of(2026,  9,  5)),
            new NormalYearReference("GANGNEUNG", PlantType.COSMOS, LocalDate.of(2026,  9, 10)),
            // 단풍
            new NormalYearReference("JEJU",      PlantType.MAPLE, LocalDate.of(2026, 10, 30)),
            new NormalYearReference("DAEJEON",   PlantType.MAPLE, LocalDate.of(2026, 11,  4)),
            new NormalYearReference("SEOUL",     PlantType.MAPLE, LocalDate.of(2026, 11,  7)),
            new NormalYearReference("GANGNEUNG", PlantType.MAPLE, LocalDate.of(2026, 11, 10))
    );

    @Override
    public List<NormalYearReference> findByPlantType(PlantType plantType) {
        return ALL.stream()
                .filter(ref -> ref.getPlantType() == plantType)
                .collect(Collectors.toList());
    }
}
