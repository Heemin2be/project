package seasonal.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import seasonal.entity.NormalYearEntity;
import seasonal.enums.PlantType;

import java.util.List;

/**
 * normal_year_reference 테이블에 대한 Spring Data JPA 리포지토리.
 */
public interface NormalYearJpaRepository extends JpaRepository<NormalYearEntity, Long> {

    List<NormalYearEntity> findByPlantType(PlantType plantType);

    boolean existsByStationCodeAndPlantType(String stationCode, PlantType plantType);
}
