package seasonal.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import seasonal.entity.ObservationCacheEntity;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.List;

/**
 * observation_cache 테이블에 대한 Spring Data JPA 리포지토리.
 * 캐시 키: (plantType, queryDate)
 */
public interface ObservationCacheJpaRepository extends JpaRepository<ObservationCacheEntity, Long> {

    /** 해당 (식물, 조회날짜) 캐시 존재 여부 */
    boolean existsByPlantTypeAndQueryDate(PlantType plantType, LocalDate queryDate);

    /** 해당 (식물, 조회날짜) 캐시 전체 반환 */
    List<ObservationCacheEntity> findByPlantTypeAndQueryDate(PlantType plantType, LocalDate queryDate);

    /** 해당 (식물, 조회날짜) 캐시 삭제 */
    @Modifying
    @Query("DELETE FROM ObservationCacheEntity e WHERE e.plantType = :plantType AND e.queryDate = :queryDate")
    void deleteByPlantTypeAndQueryDate(PlantType plantType, LocalDate queryDate);
}
