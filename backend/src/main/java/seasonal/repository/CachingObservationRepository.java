package seasonal.repository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import seasonal.domain.ObservationRecord;
import seasonal.entity.ObservationCacheEntity;
import seasonal.enums.PlantType;
import seasonal.repository.jpa.ObservationCacheJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * DB 캐시를 우선 조회하고, 없으면 KMA API를 호출한 뒤 결과를 DB에 저장합니다.
 * 데코레이터(Decorator) 패턴 — KmaPhenologyRepository를 래핑합니다.
 *
 * <p>캐시 키: (plantType, queryDate)
 * <ul>
 *   <li>같은 날짜 재조회 → DB 캐시 반환</li>
 *   <li>새 날짜 조회 → API 호출 후 저장 (당일 새 이벤트 반영)</li>
 * </ul>
 *
 * <pre>
 *   ObservationRepository (interface)
 *       ├── KmaPhenologyRepository   (API 직접 호출)
 *       └── CachingObservationRepository  ← 이 클래스 (@Primary)
 *               └── wraps KmaPhenologyRepository
 * </pre>
 */
@Primary
@Repository
public class CachingObservationRepository implements ObservationRepository {

    private static final Logger log = Logger.getLogger(CachingObservationRepository.class.getName());

    private final KmaPhenologyRepository apiRepository;
    private final ObservationCacheJpaRepository cacheJpaRepository;

    public CachingObservationRepository(
            KmaPhenologyRepository apiRepository,
            ObservationCacheJpaRepository cacheJpaRepository
    ) {
        this.apiRepository = apiRepository;
        this.cacheJpaRepository = cacheJpaRepository;
    }

    /**
     * (plantType, cacheKey) 캐시가 DB에 있으면 반환, 없으면 API 호출 후 저장합니다.
     * <ul>
     *   <li>당해 연도: queryDate 그대로 캐시 키 — 날짜별 최신 이벤트 반영</li>
     *   <li>과거 연도: YYYY-01-01 고정 캐시 키 — 확정 데이터는 연 단위 1회만 저장</li>
     * </ul>
     */
    @Override
    @Transactional
    public List<ObservationRecord> findByPlantType(PlantType plantType, LocalDate queryDate) {
        LocalDate cacheKey = toCacheKey(queryDate);

        // 1. 캐시 히트
        if (cacheJpaRepository.existsByPlantTypeAndQueryDate(plantType, cacheKey)) {
            log.info("[Cache] DB 캐시 반환 [" + plantType + " / " + cacheKey + "]");
            return cacheJpaRepository.findByPlantTypeAndQueryDate(plantType, cacheKey).stream()
                    .map(this::toDomain)
                    .toList();
        }

        // 2. 캐시 미스 → API 호출
        log.info("[Cache] 캐시 없음 → KMA API 호출 [" + plantType + " / " + queryDate + "]");
        List<ObservationRecord> records = apiRepository.findByPlantType(plantType, queryDate);

        // 3. DB 저장 (빈 결과 미캐싱 → TROUBLESHOOTING #5 참고)
        if (!records.isEmpty()) {
            List<ObservationCacheEntity> entities = records.stream()
                    .map(r -> toEntity(r, cacheKey))
                    .toList();
            cacheJpaRepository.saveAll(entities);
            log.info("[Cache] " + entities.size() + "건 저장됨 [" + plantType + " / " + cacheKey + "]");
        } else {
            log.info("[Cache] 빈 결과 — 저장 생략 [" + plantType + " / " + cacheKey + "]");
        }

        return records;
    }

    /**
     * DB 캐시 키 정규화.
     * 과거 연도는 YYYY-01-01로 고정해 연 단위 캐시를 공유합니다.
     */
    private LocalDate toCacheKey(LocalDate queryDate) {
        if (queryDate.getYear() < LocalDate.now().getYear()) {
            return LocalDate.of(queryDate.getYear(), 1, 1);
        }
        return queryDate;
    }

    private ObservationRecord toDomain(ObservationCacheEntity entity) {
        return new ObservationRecord(
                entity.getStationCode(),
                entity.getPlantType(),
                entity.getObservedDate(),
                entity.getStage(),
                entity.getSourceType()
        );
    }

    private ObservationCacheEntity toEntity(ObservationRecord record, LocalDate queryDate) {
        return new ObservationCacheEntity(
                record.getStationCode(),
                record.getPlantType(),
                queryDate,
                record.getObservedDate(),
                record.getStage(),
                record.getSourceType()
        );
    }
}
