package seasonal.repository;

import org.springframework.stereotype.Repository;
import seasonal.domain.GeoPoint;
import seasonal.domain.ObservationStation;
import seasonal.entity.StationEntity;
import seasonal.repository.jpa.StationJpaRepository;

import java.util.List;

/**
 * DB(MySQL)에서 관측소 정보를 조회하는 StationRepository 구현체.
 * StationRepository 인터페이스를 통한 다형성 활용 예시.
 */
@Repository
public class JpaStationRepository implements StationRepository {

    private final StationJpaRepository jpaRepository;

    public JpaStationRepository(StationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ObservationStation> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private ObservationStation toDomain(StationEntity entity) {
        return new ObservationStation(
                entity.getStationCode(),
                entity.getName(),
                new GeoPoint(entity.getLatitude(), entity.getLongitude()),
                entity.getAddress()
        );
    }
}
