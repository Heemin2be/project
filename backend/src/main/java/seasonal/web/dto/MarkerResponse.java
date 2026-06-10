package seasonal.web.dto;

public record MarkerResponse(
        String stationCode,
        String stationName,
        String address,
        double latitude,
        double longitude,
        String stage,
        String stageName,
        String stageColor,
        String comparison,
        String comparisonName,
        String sourceType,
        String sourceName,
        String observedDate,
        Double temperature
) {
}
