package seasonal.repository;

import org.springframework.stereotype.Repository;
import seasonal.client.AbstractKmaApiClient;
import seasonal.config.KmaApiKeyConfig;
import seasonal.domain.ObservationRecord;
import seasonal.enums.DataSourceType;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 기상청 API Hub (apihub.kma.go.kr) 계절관측 데이터 조회.
 * - 엔드포인트: /api/typ01/url/sfc_ssn.php
 * - 인증: authKey 쿼리 파라미터
 * - 응답: CSV 텍스트 (YY, STN, TM, SSN_ID, SSN_MD)
 *
 * AbstractKmaApiClient를 상속해 공통 HTTP 로직을 재사용합니다.
 */
@Repository
public class KmaPhenologyRepository extends AbstractKmaApiClient implements ObservationRepository {

    private static final Logger log = Logger.getLogger(KmaPhenologyRepository.class.getName());
    private static final String API_URL =
            "https://apihub.kma.go.kr/api/typ01/url/sfc_ssn.php";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ── KMA 지점번호 → 프로젝트 관측소 코드 ─────────────────────
    private static final Map<String, String> STATION_MAP = new HashMap<>();
    static {
        STATION_MAP.put("93",  "BUKGANGNEUNG");
        STATION_MAP.put("104", "HONGCHEON");
        STATION_MAP.put("108", "SEOUL");
        STATION_MAP.put("112", "INCHEON");
        STATION_MAP.put("115", "ULLEUNGDO");
        STATION_MAP.put("119", "SUWON");
        STATION_MAP.put("131", "CHEONGJU");
        STATION_MAP.put("133", "DAEJEON");
        STATION_MAP.put("136", "ANDONG");
        STATION_MAP.put("138", "POHANG");
        STATION_MAP.put("143", "DAEGU");
        STATION_MAP.put("146", "JEONJU");
        STATION_MAP.put("152", "ULSAN");
        STATION_MAP.put("155", "CHANGWON");
        STATION_MAP.put("156", "GWANGJU");
        STATION_MAP.put("159", "BUSAN");
        STATION_MAP.put("165", "YEOSU");
        STATION_MAP.put("172", "JINJU");
        STATION_MAP.put("184", "JEJU");
        STATION_MAP.put("189", "SEOGWIPO");
    }

    // ── SSN_ID (계절관측 코드) → PlantType ───────────────────────
    // 202: 매화, 203: 개나리, 204: 진달래, 205/207/208: 벚나무류, 302: 단풍
    private static final Map<Integer, PlantType> PLANT_BY_SSN_ID = new HashMap<>();
    static {
        PLANT_BY_SSN_ID.put(202, PlantType.MAEHWA);
        PLANT_BY_SSN_ID.put(203, PlantType.FORSYTHIA);       // 개나리
        PLANT_BY_SSN_ID.put(204, PlantType.AZALEA);          // 진달래
        PLANT_BY_SSN_ID.put(205, PlantType.CHERRY_BLOSSOM);  // 벚나무(소메이)
        PLANT_BY_SSN_ID.put(207, PlantType.CHERRY_BLOSSOM);  // 벚나무
        PLANT_BY_SSN_ID.put(208, PlantType.CHERRY_BLOSSOM);  // 왕벚나무
        PLANT_BY_SSN_ID.put(302, PlantType.MAPLE);           // 단풍
    }

    // ── SSN_MD (계절현상 코드) → PhenologyStage ──────────────────
    // 201: 개화/시작, 202: 만개/절정, 203: 낙화/낙엽
    private static final Map<Integer, PhenologyStage> STAGE_BY_SSN_MD = new HashMap<>();
    static {
        STAGE_BY_SSN_MD.put(201, PhenologyStage.STARTED);
        STAGE_BY_SSN_MD.put(202, PhenologyStage.FULL_BLOOM);
        STAGE_BY_SSN_MD.put(203, PhenologyStage.ENDED);
        STAGE_BY_SSN_MD.put(301, PhenologyStage.STARTED);   // 단풍 시작
        STAGE_BY_SSN_MD.put(302, PhenologyStage.PEAK);      // 단풍 절정
        STAGE_BY_SSN_MD.put(303, PhenologyStage.ENDED);     // 낙엽
    }

    public KmaPhenologyRepository(KmaApiKeyConfig kmaApiKeyConfig) {
        super(kmaApiKeyConfig);
    }

    @Override
    public List<ObservationRecord> findByPlantType(PlantType plantType, LocalDate queryDate) {
        if (!hasApiKey()) {
            throw new IllegalStateException(
                    "KMA API 키가 설정되지 않았습니다. .env의 KMA_WEATHER.API_KEY를 확인하세요.");
        }
        try {
            // queryDate의 연도 기준으로 조회 — 과거 날짜 조회 시 해당 연도 데이터를 가져옴
            int year = queryDate.getYear();
            List<ObservationRecord> records = fetchForYear(year, plantType);
            if (records.isEmpty()) {
                // 해당 연도 데이터 없음 (비시즌 또는 연초) → 전년도 폴백
                log.info("[KMA 계절관측] " + year + "년 데이터 없음 → " + (year - 1) + "년 폴백 [" + plantType + "]");
                records = fetchForYear(year - 1, plantType);
            }
            log.info("[KMA 계절관측] " + records.size() + "건 로드됨 [" + plantType + "]");
            return records;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "KMA 계절관측 API 호출 실패 [" + plantType + "]: " + e.getMessage(), e);
        }
    }

    private List<ObservationRecord> fetchForYear(int year, PlantType plantType) throws Exception {
        String tm1 = year + "0101";
        String tm2 = year + "1231";
        String url = API_URL
                + "?stn=0"
                + "&tm1=" + tm1
                + "&tm2=" + tm2
                + "&authKey=" + apiKey;

        var response = get(url);
        String body = response.body();
        log.info("[KMA 계절관측] HTTP " + response.statusCode() + " [" + year + "] "
                + body.substring(0, Math.min(200, body.length())));

        return parseRecords(body, plantType);
    }

    // ── 텍스트 파싱 ────────────────────────────────────────────────

    List<ObservationRecord> parseRecords(String text, PlantType targetPlant) {
        List<ObservationRecord> result = new ArrayList<>();
        if (text == null || text.isBlank()) return result;

        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            parseRecord(line, targetPlant).ifPresent(result::add);
        }
        return result;
    }

    /**
     * 한 줄 파싱: " 2026,  108,  2026-03-30,   208,   201,"
     * 필드: YY, STN, TM, SSN_ID, SSN_MD
     */
    private Optional<ObservationRecord> parseRecord(String line, PlantType targetPlant) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 5) return Optional.empty();

            String stnStr = parts[1].trim();
            String stationCode = STATION_MAP.get(stnStr);
            if (stationCode == null) return Optional.empty();  // 관심 지점 외

            int ssnId = Integer.parseInt(parts[3].trim());
            PlantType plantType = PLANT_BY_SSN_ID.get(ssnId);
            if (plantType == null || plantType != targetPlant) return Optional.empty();

            int ssnMd = Integer.parseInt(parts[4].trim());
            PhenologyStage stage = STAGE_BY_SSN_MD.get(ssnMd);
            if (stage == null) return Optional.empty();

            String tmStr = parts[2].trim();  // "2026-03-30"
            LocalDate date = LocalDate.parse(tmStr, DATE_FMT);

            return Optional.of(new ObservationRecord(
                    stationCode, plantType, date, stage, DataSourceType.API
            ));
        } catch (Exception e) {
            log.fine("라인 파싱 실패: [" + line + "] " + e.getMessage());
            return Optional.empty();
        }
    }
}
