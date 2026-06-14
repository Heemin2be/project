package seasonal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import seasonal.enums.DataSourceType;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * KMA API 관측 결과를 DB에 캐싱하는 JPA 엔티티.
 *
 * 캐시 키: (plantType, queryDate)
 * — 사용자가 조회한 날짜별로 별도 캐시를 유지합니다.
 * — 같은 날짜 재조회는 DB에서 반환, 다른 날짜는 API를 다시 호출합니다.
 */
@Entity
@Table(name = "observation_cache")
public class ObservationCacheEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code", nullable = false, length = 20)
    private String stationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "plant_type", nullable = false, length = 30)
    private PlantType plantType;

    /** 사용자가 조회한 날짜 — 캐시 키의 일부 */
    @Column(name = "query_date", nullable = false)
    private LocalDate queryDate;

    @Column(name = "observed_date", nullable = false)
    private LocalDate observedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PhenologyStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private DataSourceType sourceType;

    @Column(name = "cached_at", nullable = false)
    private LocalDateTime cachedAt;

    protected ObservationCacheEntity() {}

    public ObservationCacheEntity(
            String stationCode,
            PlantType plantType,
            LocalDate queryDate,
            LocalDate observedDate,
            PhenologyStage stage,
            DataSourceType sourceType
    ) {
        this.stationCode = stationCode;
        this.plantType = plantType;
        this.queryDate = queryDate;
        this.observedDate = observedDate;
        this.stage = stage;
        this.sourceType = sourceType;
        this.cachedAt = LocalDateTime.now();
    }

    public Long getId()               { return id; }
    public String getStationCode()    { return stationCode; }
    public PlantType getPlantType()   { return plantType; }
    public LocalDate getQueryDate()   { return queryDate; }
    public LocalDate getObservedDate(){ return observedDate; }
    public PhenologyStage getStage()  { return stage; }
    public DataSourceType getSourceType() { return sourceType; }
    public LocalDateTime getCachedAt()    { return cachedAt; }
}
