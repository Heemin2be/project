package seasonal.weather;

import org.springframework.stereotype.Service;
import seasonal.config.KmaApiKeyConfig;
import seasonal.domain.ObservationStation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 기상청 초단기실황 API (getUltraSrtNcst)를 호출해 각 관측소의 현재 기온(T1H)을 조회합니다.
 * 결과는 15분간 캐싱하며, API 실패 시 빈 Map을 반환해 서비스 장애가 전파되지 않도록 합니다.
 * (기온은 보조 정보이므로 best-effort로 처리합니다.)
 */
@Service
public class KmaWeatherService {

    private static final Logger log = Logger.getLogger(KmaWeatherService.class.getName());
    private static final String API_URL =
            "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";
    private static final long CACHE_TTL_MINUTES = 15;

    /**
     * 관측소 코드 → KMA 동네예보 격자 (nx, ny).
     * 기상청 격자 변환표 기준 (위경도 → Lambert 격자).
     */
    private static final Map<String, int[]> STATION_GRID = Map.of(
            "SEOUL",     new int[]{60, 127},
            "GANGNEUNG", new int[]{92, 131},
            "DAEJEON",   new int[]{67, 100},
            "DAEGU",     new int[]{89,  90},
            "GWANGJU",   new int[]{58,  74},
            "BUSAN",     new int[]{98,  76},
            "JEJU",      new int[]{52,  38}
    );

    private final String apiKey;
    private final HttpClient httpClient;

    private volatile Map<String, Double> cachedTemps = Map.of();
    private volatile LocalDateTime cacheTime = LocalDateTime.MIN;

    public KmaWeatherService(KmaApiKeyConfig kmaApiKeyConfig) {
        this.apiKey = kmaApiKeyConfig.loadApiKey();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 주어진 관측소 목록의 현재 기온을 반환합니다.
     * API 키가 비어 있거나 호출 실패 시 빈 Map을 반환합니다.
     *
     * @return 관측소 코드 → 기온(℃) Map
     */
    public Map<String, Double> fetchTemperatures(List<ObservationStation> stations) {
        if (apiKey == null || apiKey.isBlank()) {
            return Map.of();
        }

        if (LocalDateTime.now().isBefore(cacheTime.plusMinutes(CACHE_TTL_MINUTES))) {
            return cachedTemps;
        }

        String[] dt = baseDateTime();
        String baseDate = dt[0];
        String baseTime = dt[1];

        Map<String, Double> result = new ConcurrentHashMap<>();
        for (ObservationStation station : stations) {
            int[] grid = STATION_GRID.get(station.getStationCode());
            if (grid == null) {
                continue;
            }
            try {
                Double temp = callApi(baseDate, baseTime, grid[0], grid[1]);
                if (temp != null) {
                    result.put(station.getStationCode(), temp);
                }
            } catch (Exception exception) {
                log.warning("기온 조회 실패 [" + station.getStationCode() + "]: " + exception.getMessage());
            }
        }

        cachedTemps = result;
        cacheTime = LocalDateTime.now();
        return result;
    }

    private Double callApi(String baseDate, String baseTime, int nx, int ny) throws Exception {
        String url = API_URL
                + "?serviceKey=" + apiKey
                + "&numOfRows=10&pageNo=1&dataType=JSON"
                + "&base_date=" + baseDate
                + "&base_time=" + baseTime
                + "&nx=" + nx
                + "&ny=" + ny;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseTemperature(response.body());
    }

    /**
     * JSON 응답에서 category=="T1H" 항목의 obsrValue를 파싱합니다.
     */
    static Double parseTemperature(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        int t1hIdx = json.indexOf("\"T1H\"");
        if (t1hIdx < 0) {
            return null;
        }
        int objEnd = json.indexOf('}', t1hIdx);
        if (objEnd < 0) {
            objEnd = json.length();
        }
        int objStart = json.lastIndexOf('{', t1hIdx);
        if (objStart < 0) {
            objStart = 0;
        }
        String item = json.substring(objStart, objEnd);

        int obsrIdx = item.indexOf("\"obsrValue\"");
        if (obsrIdx < 0) {
            return null;
        }
        int colonIdx = item.indexOf(':', obsrIdx + 11);
        if (colonIdx < 0) {
            return null;
        }
        int valStart = item.indexOf('"', colonIdx) + 1;
        int valEnd = item.indexOf('"', valStart);
        if (valStart <= 0 || valEnd < 0) {
            return null;
        }
        try {
            return Double.parseDouble(item.substring(valStart, valEnd).trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * KMA API base_date, base_time을 계산합니다.
     * 초단기실황은 매 시각 45분 이후 전 시각 데이터가 확정되므로,
     * 현재 분이 45 미만이면 1시간 전을 사용합니다.
     */
    static String[] baseDateTime() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getMinute() < 45) {
            now = now.minusHours(1);
        }
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String time = now.format(DateTimeFormatter.ofPattern("HH")) + "00";
        return new String[]{date, time};
    }
}
