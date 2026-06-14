package seasonal.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import seasonal.entity.StationEntity;

/**
 * station 테이블에 대한 Spring Data JPA 리포지토리.
 */
public interface StationJpaRepository extends JpaRepository<StationEntity, String> {
}
