package seasonal.map;

import seasonal.domain.GeoPoint;

import java.awt.Point;

public interface MapProjection {
    Point project(GeoPoint point, MapViewport viewport);
}
