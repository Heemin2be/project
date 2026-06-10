package seasonal.repository;

import org.springframework.stereotype.Repository;
import seasonal.domain.GeoPoint;
import seasonal.domain.ObservationStation;

import java.util.List;

@Repository
public class CsvStationRepository implements StationRepository {

    private static final List<ObservationStation> STATIONS = List.of(
            new ObservationStation("SEOUL",     "서울", new GeoPoint(37.5665, 126.9780), "서울특별시 중구"),
            new ObservationStation("GANGNEUNG", "강릉", new GeoPoint(37.7519, 128.8761), "강원특별자치도 강릉시"),
            new ObservationStation("DAEJEON",   "대전", new GeoPoint(36.3504, 127.3845), "대전광역시"),
            new ObservationStation("DAEGU",     "대구", new GeoPoint(35.8714, 128.6014), "대구광역시"),
            new ObservationStation("GWANGJU",   "광주", new GeoPoint(35.1595, 126.8526), "광주광역시"),
            new ObservationStation("BUSAN",     "부산", new GeoPoint(35.1796, 129.0756), "부산광역시"),
            new ObservationStation("JEJU",      "제주", new GeoPoint(33.4996, 126.5312), "제주특별자치도 제주시")
    );

    @Override
    public List<ObservationStation> findAll() {
        return STATIONS;
    }
}
