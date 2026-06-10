package seasonal.map;

import org.junit.jupiter.api.Test;
import seasonal.domain.GeoPoint;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebMercatorProjectionTest {
    @Test
    void projectsCenterNearViewportCenter() {
        MapViewport viewport = new MapViewport(
                new GeoPoint(38.7, 125.0),
                new GeoPoint(33.0, 130.4),
                1000,
                800
        );

        Point point = new WebMercatorProjection().project(new GeoPoint(35.9, 127.7), viewport);

        assertEquals(500, point.x, 2);
        assertEquals(400, point.y, 15);
    }
}
