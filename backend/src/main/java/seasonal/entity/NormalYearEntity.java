package seasonal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import seasonal.enums.PlantType;

import java.time.LocalDate;

/**
 * 식물별 관측소 평년 기준일을 저장하는 JPA 엔티티.
 * (station_code, plant_type) 조합은 유일합니다.
 */
@Entity
@Table(
    name = "normal_year_reference",
    uniqueConstraints = @UniqueConstraint(columnNames = {"station_code", "plant_type"})
)
public class NormalYearEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code", nullable = false, length = 20)
    private String stationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "plant_type", nullable = false, length = 30)
    private PlantType plantType;

    @Column(name = "normal_date", nullable = false)
    private LocalDate normalDate;

    protected NormalYearEntity() {}

    public NormalYearEntity(String stationCode, PlantType plantType, LocalDate normalDate) {
        this.stationCode = stationCode;
        this.plantType = plantType;
        this.normalDate = normalDate;
    }

    public Long getId()            { return id; }
    public String getStationCode() { return stationCode; }
    public PlantType getPlantType(){ return plantType; }
    public LocalDate getNormalDate(){ return normalDate; }
}
