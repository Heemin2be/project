package seasonal.web.dto;

import java.util.List;

public record MapViewResponse(
        String plantType,
        String plantName,
        String queryDate,
        List<MarkerResponse> markers
) {
}
