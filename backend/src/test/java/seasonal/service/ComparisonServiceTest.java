package seasonal.service;

import org.junit.jupiter.api.Test;
import seasonal.enums.ComparisonResultType;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparisonServiceTest {
    private final ComparisonService service = new ComparisonService();
    private final LocalDate normalDate = LocalDate.of(2026, 4, 10);

    @Test
    void comparesEarlyNormalAndLate() {
        assertEquals(ComparisonResultType.EARLY, service.compare(LocalDate.of(2026, 4, 6), normalDate));
        assertEquals(ComparisonResultType.NORMAL, service.compare(LocalDate.of(2026, 4, 7), normalDate));
        assertEquals(ComparisonResultType.NORMAL, service.compare(LocalDate.of(2026, 4, 13), normalDate));
        assertEquals(ComparisonResultType.LATE, service.compare(LocalDate.of(2026, 4, 14), normalDate));
    }
}
