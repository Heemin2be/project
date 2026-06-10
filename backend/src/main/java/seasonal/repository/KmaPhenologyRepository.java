package seasonal.repository;

import seasonal.domain.ObservationRecord;
import seasonal.enums.DataSourceType;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 기상청 생물계절관측 API (PhnlgObsSvc / getPhnlgObs)로 관측 데이터를 조회합니다.
 * API 키 미설정·호출 실패·데이터 없음의 경우 CsvObservationRepository로 폴백합니다.
 *
 * <p>KMA 응답 예시:
 * <pre>
 * { "response": { "body": { "items": { "item": [
 *   { "stnId":108, "stnNm":"서울", "tm":"20260403",
 *     "phnlgKorNm":"벚꽃", "stageKorNm":"개화" }, ...
 * ] } } } }
 * </pre>
 */
public class KmaPhenologyRepository implements ObservationRepository {

    private static final Logger log = Logger.getLogger(KmaPhenologyRepository.class.getName());
    private static final String API_URL =
            "https://apis.data.go.kr/1360000/PhnlgObsSvc/getPhnlgObs";
    private static final DateTimeFormatter KMA_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** KMA 지점번호 → 프로젝트 관측소 코드 */
    private static final Map<String, String> STATION_MAP;
    static {
        STATION_MAP = new HashMap<>();
        STATION_MAP.put("108", "SEOUL");
        STATION_MAP.put("105", "GANGNEUNG");
        STATION_MAP.put("133", "DAEJEON");
        STATION_MAP.put("143", "DAEGU");
        STATION_MAP.put("156", "GWANGJU");
        STATION_MAP.put("159", "BUSAN");
        STATION_MAP.put("184", "JEJU");
    }

    /** KMA 식물 한국어명 → PlantType */
    private static final Map<String, PlantType> PLANT_MAP;
    static {
        PLANT_MAP = new HashMap<>();
        PLANT_MAP.put("벚꽃",    PlantType.CHERRY_BLOSSOM);
        PLANT_MAP.put("벚나무",  PlantType.CHERRY_BLOSSOM);
        PLANT_MAP.put("매화",    PlantType.MAEHWA);
        PLANT_MAP.put("코스모스", PlantType.COSMOS);
        PLANT_MAP.put("단풍나무", PlantType.MAPLE);
    }

    /** KMA 관측단계 한국어명 → PhenologyStage */
    private static final Map<String, PhenologyStage> STAGE_MAP;
    static {
        STAGE_MAP = new HashMap<>();
        // 개화 계열
        STAGE_MAP.put("개화",    PhenologyStage.STARTED);
        STAGE_MAP.put("만개",    PhenologyStage.FULL_BLOOM);
        STAGE_MAP.put("낙화",    PhenologyStage.ENDED);
        // 단풍 계열
        STAGE_MAP.put("단풍시작", PhenologyStage.STARTED);
        STAGE_MAP.put("단풍절정", PhenologyStage.PEAK);
        STAGE_MAP.put("낙엽",    PhenologyStage.ENDED);
        // 그 외 혹시 모를 표기 변형
        STAGE_MAP.put("시작",    PhenologyStage.STARTED);
        STAGE_MAP.put("절정",    PhenologyStage.PEAK);
        STAGE_MAP.put("종료",    PhenologyStage.ENDED);
    }

    private final String apiKey;
    private final ObservationRepository csvFallback;
    private final HttpClient httpClient;

    public KmaPhenologyRepository(String apiKey) {
        this(apiKey, new CsvObservationRepository());
    }

    KmaPhenologyRepository(String apiKey, ObservationRepository csvFallback) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.csvFallback = csvFallback;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public List<ObservationRecord> findByPlantType(PlantType plantType) {
        // API 키 없음 → 개발용 CSV 사용
        if (apiKey.isBlank()) {
            return csvFallback.findByPlantType(plantType);
        }
        // API 키 있음 → API 우선, 예외 시 빈 결과 반환
        try {
            int year = LocalDate.now().getYear();
            List<ObservationRecord> records = fetchForYear(year, plantType);
            if (records.isEmpty()) {
                records = fetchForYear(year - 1, plantType);
            }
            if (records.isEmpty()) {
                // API 정상 응답이지만 데이터 없음 → CSV 폴백
                System.out.println("[KMA 계절관측] API 데이터 없음 → CSV 사용 [" + plantType + "]");
                return csvFallback.findByPlantType(plantType);
            }
            System.out.println("[KMA 계절관측] " + records.size() + "건 로드됨 [" + plantType + "]");
            return records;
        } catch (Exception exception) {
            // API 오류(연결 실패, 타임아웃 등) → 빈 결과 반환
            System.out.println("[KMA 계절관측] API 오류 → 마커 없음 [" + plantType + "]: " + exception.getMessage());
            return List.of();
        }
    }

    private List<ObservationRecord> fetchForYear(int year, PlantType plantType) throws Exception {
        String url = API_URL
                + "?serviceKey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&pageNo=1&numOfRows=1000&dataType=JSON"
                + "&year=" + year;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status != 200) {
            System.out.println("[KMA 계절관측] HTTP " + status + " 응답 [" + year + "]");
        }
        return parseRecords(response.body(), plantType);
    }

    // ── JSON 파싱 ──────────────────────────────────────────────────

    List<ObservationRecord> parseRecords(String json, PlantType targetPlant) {
        List<ObservationRecord> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;

        // 오류 응답 확인 ("resultCode" != "00")
        if (json.contains("\"resultCode\"") && !json.contains("\"00\"")) {
            System.out.println("[KMA 계절관측] API 오류 응답: " + json.substring(0, Math.min(300, json.length())));
            return result;
        }

        for (Map<String, String> fields : parseItems(json)) {
            mapToRecord(fields, targetPlant).ifPresent(result::add);
        }
        return result;
    }

    private Optional<ObservationRecord> mapToRecord(Map<String, String> fields, PlantType targetPlant) {
        try {
            // 지점 매핑 (stnId 숫자 또는 문자열)
            String stnId = fields.getOrDefault("stnId", "").trim()
                    .replaceAll("\\.0$", ""); // 108.0 → 108
            String stationCode = STATION_MAP.get(stnId);
            if (stationCode == null) return Optional.empty();

            // 식물 매핑
            String plantName = firstNonEmpty(fields, "phnlgKorNm", "phnlgNm");
            PlantType plantType = PLANT_MAP.get(plantName);
            if (plantType == null || plantType != targetPlant) return Optional.empty();

            // 단계 매핑
            String stageName = firstNonEmpty(fields, "stageKorNm", "stageNm", "phnlgStgNm");
            PhenologyStage stage = STAGE_MAP.get(stageName);
            if (stage == null) return Optional.empty();

            // 관측일 (tm: YYYYMMDD)
            String tm = fields.getOrDefault("tm", "").trim();
            if (tm.length() != 8) return Optional.empty();
            LocalDate date = LocalDate.parse(tm, KMA_DATE);

            return Optional.of(new ObservationRecord(
                    stationCode, plantType, date, stage, DataSourceType.API
            ));
        } catch (Exception exception) {
            log.fine("레코드 파싱 실패: " + fields + " / " + exception.getMessage());
            return Optional.empty();
        }
    }

    private static String firstNonEmpty(Map<String, String> fields, String... keys) {
        for (String key : keys) {
            String v = fields.get(key);
            if (v != null && !v.isBlank()) return v.trim();
        }
        return "";
    }

    /**
     * JSON 응답에서 item 배열의 각 객체를 파싱합니다.
     * 라이브러리 없이 단순 문자열 처리로 구현합니다.
     */
    static List<Map<String, String>> parseItems(String json) {
        List<Map<String, String>> items = new ArrayList<>();
        int itemIdx = json.indexOf("\"item\"");
        if (itemIdx < 0) return items;

        int arrayStart = json.indexOf('[', itemIdx);
        int arrayEnd = json.lastIndexOf(']');
        if (arrayStart < 0 || arrayEnd <= arrayStart) return items;

        String content = json.substring(arrayStart + 1, arrayEnd);
        int depth = 0;
        int objStart = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (depth == 0) objStart = i + 1;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    items.add(parseObject(content.substring(objStart, i)));
                    objStart = -1;
                }
            }
        }
        return items;
    }

    private static final Pattern FIELD_PATTERN =
            Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|(-?[\\d.]+|true|false|null))");

    static Map<String, String> parseObject(String obj) {
        Map<String, String> fields = new HashMap<>();
        Matcher m = FIELD_PATTERN.matcher(obj);
        while (m.find()) {
            String key = m.group(1);
            String value = m.group(2) != null ? m.group(2) : m.group(3);
            if (value != null) fields.put(key, value.trim());
        }
        return fields;
    }
}
