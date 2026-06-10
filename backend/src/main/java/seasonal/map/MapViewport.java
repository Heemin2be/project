package seasonal.map;

import seasonal.domain.GeoPoint;

import java.util.Objects;

public final class MapViewport {
    private final GeoPoint northWest;
    private final GeoPoint southEast;
    private final int width;
    private final int height;

    public MapViewport(GeoPoint northWest, GeoPoint southEast, int width, int height) {
        this.northWest = Objects.requireNonNull(northWest, "북서쪽 좌표는 null일 수 없습니다.");
        this.southEast = Objects.requireNonNull(southEast, "남동쪽 좌표는 null일 수 없습니다.");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("화면 크기는 0보다 커야 합니다.");
        }
        this.width = width;
        this.height = height;
    }

    public GeoPoint getNorthWest() {
        return northWest;
    }

    public GeoPoint getSouthEast() {
        return southEast;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
