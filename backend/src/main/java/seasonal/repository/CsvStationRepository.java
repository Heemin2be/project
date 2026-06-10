package seasonal.repository;

import seasonal.domain.GeoPoint;
import seasonal.domain.ObservationStation;

import java.util.List;

public class CsvStationRepository implements StationRepository {
    @Override
    public List<ObservationStation> findAll() {
        return CsvReader.readRows("/data/stations.csv").stream()
                .map(row -> new ObservationStation(
                        row[0],
                        row[1],
                        new GeoPoint(Double.parseDouble(row[2]), Double.parseDouble(row[3])),
                        row[4]
                ))
                .toList();
    }
}
