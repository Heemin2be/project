package seasonal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 관측소 정보를 DB에 저장하는 JPA 엔티티.
 * station_code를 기본 키로 사용합니다.
 */
@Entity
@Table(name = "station")
public class StationEntity {

    @Id
    @Column(name = "station_code", length = 20)
    private String stationCode;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(length = 100)
    private String address;

    protected StationEntity() {}

    public StationEntity(String stationCode, String name, double latitude, double longitude, String address) {
        this.stationCode = stationCode;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public String getStationCode() { return stationCode; }
    public String getName()        { return name; }
    public double getLatitude()    { return latitude; }
    public double getLongitude()   { return longitude; }
    public String getAddress()     { return address; }
}
