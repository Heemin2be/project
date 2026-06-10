package seasonal.map;

public interface MapProvider {
    String getName();

    String createJavaScriptApiUrl(String apiKey);
}
