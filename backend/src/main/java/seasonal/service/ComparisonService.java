package seasonal.service;

import org.springframework.stereotype.Service;
import seasonal.enums.ComparisonResultType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ComparisonService {
    private static final long NORMAL_RANGE_DAYS = 3;

    public ComparisonResultType compare(LocalDate observedDate, LocalDate normalDate) {
        if (observedDate == null || normalDate == null) {
            return ComparisonResultType.UNKNOWN;
        }

        // 연도가 다를 수 있으므로 (평년 기준일은 2026 고정, 과거 조회는 2025 등)
        // normalDate를 observedDate의 연도로 정규화해 월/일 기준으로만 비교
        LocalDate normalDateNormalized = normalDate.withYear(observedDate.getYear());
        long difference = ChronoUnit.DAYS.between(normalDateNormalized, observedDate);
        if (difference < -NORMAL_RANGE_DAYS) {
            return ComparisonResultType.EARLY;
        }
        if (difference > NORMAL_RANGE_DAYS) {
            return ComparisonResultType.LATE;
        }
        return ComparisonResultType.NORMAL;
    }
}
