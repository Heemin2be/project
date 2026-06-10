package seasonal.repository;

import seasonal.domain.ObservationStation;

import java.util.List;

public interface StationRepository {
    List<ObservationStation> findAll();
}
