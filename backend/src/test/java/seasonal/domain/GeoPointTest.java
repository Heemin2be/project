package seasonal.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeoPointTest {
    @Test
    void createsValidCoordinate() {
        GeoPoint point = new GeoPoint(37.5665, 126.9780);

        assertEquals(37.5665, point.getLatitude());
        assertEquals(126.9780, point.getLongitude());
    }

    @Test
    void rejectsInvalidCoordinate() {
        assertThrows(IllegalArgumentException.class, () -> new GeoPoint(91, 127));
        assertThrows(IllegalArgumentException.class, () -> new GeoPoint(37, 181));
    }
}
