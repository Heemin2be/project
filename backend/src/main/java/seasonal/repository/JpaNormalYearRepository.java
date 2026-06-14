package seasonal.repository;

import org.springframework.stereotype.Repository;
import seasonal.domain.NormalYearReference;
import seasonal.entity.NormalYearEntity;
import seasonal.enums.PlantType;
import seasonal.repository.jpa.NormalYearJpaRepository;

import java.util.List;

/**
 * DB(MySQL)에서 평년 기준일을 조회하는 NormalYearRepository 구현체.
 */
@Repository
public class JpaNormalYearRepository implements NormalYearRepository {

    private final NormalYearJpaRepository jpaRepository;

    public JpaNormalYearRepository(NormalYearJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<NormalYearReference> findByPlantType(PlantType plantType) {
        return jpaRepository.findByPlantType(plantType).stream()
                .map(this::toDomain)
                .toList();
    }

    private NormalYearReference toDomain(NormalYearEntity entity) {
        return new NormalYearReference(
                entity.getStationCode(),
                entity.getPlantType(),
                entity.getNormalDate()
        );
    }
}
