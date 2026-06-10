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

        long difference = ChronoUnit.DAYS.between(normalDate, observedDate);
        if (difference < -NORMAL_RANGE_DAYS) {
            return ComparisonResultType.EARLY;
        }
        if (difference > NORMAL_RANGE_DAYS) {
            return ComparisonResultType.LATE;
        }
        return ComparisonResultType.NORMAL;
    }
}
