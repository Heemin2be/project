package seasonal.config;

public class KmaApiKeyConfig {
    /** 시스템 환경 변수 이름 (셸에서는 점 사용 불가 → 언더스코어 변형) */
    public static final String ENV_NAME = "KMA_WEATHER_API_KEY";
    /** .env 파일에서 사용하는 키 이름 */
    static final String DOT_ENV_KEY = "KMA_WEATHER.API_KEY";

    public String loadApiKey() {
        String sysEnv = System.getenv(ENV_NAME);
        if (sysEnv != null && !sysEnv.isBlank()) {
            return sysEnv.trim();
        }

        String dotEnvValue = DotEnvReader.read(DOT_ENV_KEY);
        return dotEnvValue != null ? dotEnvValue : "";
    }
}
