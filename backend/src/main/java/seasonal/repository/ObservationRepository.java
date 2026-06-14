package seasonal.repository;

import seasonal.domain.ObservationRecord;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.util.List;

public interface ObservationRepository {
    /**
     * 해당 식물의 관측 기록을 반환합니다.
     * @param queryDate 사용자 조회 날짜 — 캐시 키로 사용되며, API 호출 범위와는 무관합니다
     */
    List<ObservationRecord> findByPlantType(PlantType plantType, LocalDate queryDate);
}
