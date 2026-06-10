package seasonal.service;

import seasonal.domain.MapMarker;
import seasonal.domain.MapViewModel;
import seasonal.domain.ObservationStation;
import seasonal.enums.PlantType;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class JsonSupport {
    private JsonSupport() {
    }

    public static String plantsJson() {
        String values = Arrays.stream(PlantType.values())
                .map(plant -> "{\"name\":\"" + plant.name() + "\",\"displayName\":\"" + escape(plant.getDisplayName()) + "\"}")
                .collect(Collectors.joining(","));
        return "[" + values + "]";
    }

    public static String mapViewJson(MapViewModel model) {
        String markers = model.getMarkers().stream()
                .map(JsonSupport::markerJson)
                .collect(Collectors.joining(","));
        return "{"
                + "\"plantType\":\"" + model.getPlantType().name() + "\","
                + "\"plantName\":\"" + escape(model.getPlantType().getDisplayName()) + "\","
                + "\"queryDate\":\"" + model.getQueryDate() + "\","
                + "\"markers\":[" + markers + "]"
                + "}";
    }

    private static String markerJson(MapMarker marker) {
        ObservationStation station = marker.getStation();
        return "{"
                + "\"stationCode\":\"" + escape(station.getStationCode()) + "\","
                + "\"stationName\":\"" + escape(station.getName()) + "\","
                + "\"address\":\"" + escape(station.getAddress()) + "\","
                + "\"latitude\":" + station.getLocation().getLatitude() + ","
                + "\"longitude\":" + station.getLocation().getLongitude() + ","
                + "\"stage\":\"" + marker.getStage().name() + "\","
                + "\"stageName\":\"" + escape(marker.getStage().getDisplayName()) + "\","
                + "\"stageColor\":\"" + escape(marker.getColor()) + "\","
                + "\"comparison\":\"" + marker.getComparison().name() + "\","
                + "\"comparisonName\":\"" + escape(marker.getComparison().getDisplayName()) + "\","
                + "\"sourceType\":\"" + marker.getSourceType().name() + "\","
                + "\"sourceName\":\"" + escape(marker.getSourceType().getDisplayName()) + "\","
                + "\"observedDate\":\"" + marker.getObservedDate() + "\","
                + "\"temperature\":" + (marker.getTemperature() != null ? marker.getTemperature() : "null")
                + "}";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
