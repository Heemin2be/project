package seasonal.map;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleMapsProvider implements MapProvider {
    @Override
    public String getName() {
        return "Google Maps";
    }

    @Override
    public String createJavaScriptApiUrl(String apiKey) {
        String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        return "https://maps.googleapis.com/maps/api/js?key=" + encodedKey
                + "&loading=async&libraries=marker&callback=__initGoogleMaps";
    }
}
