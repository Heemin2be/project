package seasonal.map;

import seasonal.domain.GeoPoint;

import java.awt.Point;

public class WebMercatorProjection implements MapProjection {
    @Override
    public Point project(GeoPoint point, MapViewport viewport) {
        double west = viewport.getNorthWest().getLongitude();
        double east = viewport.getSouthEast().getLongitude();
        double north = mercatorY(viewport.getNorthWest().getLatitude());
        double south = mercatorY(viewport.getSouthEast().getLatitude());

        double xRatio = (point.getLongitude() - west) / (east - west);
        double yRatio = (north - mercatorY(point.getLatitude())) / (north - south);

        return new Point(
                (int) Math.round(xRatio * viewport.getWidth()),
                (int) Math.round(yRatio * viewport.getHeight())
        );
    }

    private double mercatorY(double latitude) {
        double radians = Math.toRadians(Math.max(-85, Math.min(85, latitude)));
        return Math.log(Math.tan(Math.PI / 4 + radians / 2));
    }
}
