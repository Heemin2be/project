package seasonal.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ApiKeyConfig {
    public static final String ENV_NAME = "GOOGLE_MAPS_API_KEY";
    private static final Path LOCAL_CONFIG = Path.of("config", "google-maps.properties");

    public String loadGoogleMapsApiKey() {
        String environmentKey = System.getenv(ENV_NAME);
        if (environmentKey != null && !environmentKey.isBlank()) {
            return environmentKey.trim();
        }

        String dotEnvKey = DotEnvReader.read(ENV_NAME);
        if (dotEnvKey != null && !dotEnvKey.isBlank()) {
            return dotEnvKey;
        }

        if (!Files.exists(LOCAL_CONFIG)) {
            return "";
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(LOCAL_CONFIG)) {
            properties.load(inputStream);
            return properties.getProperty("google.maps.apiKey", "").trim();
        } catch (IOException exception) {
            throw new ApiRequestException("Google Maps API 키 설정을 읽지 못했습니다.", exception);
        }
    }
}
