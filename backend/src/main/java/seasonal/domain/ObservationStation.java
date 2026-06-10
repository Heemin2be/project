package seasonal.domain;

import java.util.Objects;

public final class ObservationStation {
    private final String stationCode;
    private final String name;
    private final GeoPoint location;
    private final String address;

    public ObservationStation(String stationCode, String name, GeoPoint location, String address) {
        this.stationCode = requireText(stationCode, "관측지점 코드는 비어 있을 수 없습니다.");
        this.name = requireText(name, "관측지점 이름은 비어 있을 수 없습니다.");
        this.location = Objects.requireNonNull(location, "좌표는 null일 수 없습니다.");
        this.address = address == null ? "" : address.trim();
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public String getStationCode() {
        return stationCode;
    }

    public String getName() {
        return name;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getAddress() {
        return address;
    }
}
