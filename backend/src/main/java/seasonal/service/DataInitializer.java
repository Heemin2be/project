package seasonal.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import seasonal.entity.NormalYearEntity;
import seasonal.entity.StationEntity;
import seasonal.enums.PlantType;
import seasonal.repository.jpa.NormalYearJpaRepository;
import seasonal.repository.jpa.StationJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * 애플리케이션 시작 시 관측소 좌표 및 평년 기준일을 DB에 적재합니다.
 * 이미 데이터가 존재하면 삽입을 생략합니다 (멱등성 보장).
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = Logger.getLogger(DataInitializer.class.getName());

    private final StationJpaRepository stationRepo;
    private final NormalYearJpaRepository normalYearRepo;

    public DataInitializer(StationJpaRepository stationRepo, NormalYearJpaRepository normalYearRepo) {
        this.stationRepo = stationRepo;
        this.normalYearRepo = normalYearRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initStations();
        initNormalYears();
    }

    // ── 관측소 초기 데이터 ─────────────────────────────────────────

    private void initStations() {
        List<StationEntity> stations = List.of(
                new StationEntity("BUKGANGNEUNG", "북강릉", 37.8042, 128.8678, "강원특별자치도 강릉시"),
                new StationEntity("HONGCHEON",    "홍천",   37.6838, 127.8794, "강원특별자치도 홍천군"),
                new StationEntity("SEOUL",        "서울",   37.5665, 126.9780, "서울특별시 중구"),
                new StationEntity("INCHEON",      "인천",   37.4775, 126.6244, "인천광역시 미추홀구"),
                new StationEntity("ULLEUNGDO",    "울릉도", 37.4770, 130.8980, "경상북도 울릉군"),
                new StationEntity("SUWON",        "수원",   37.2681, 127.0065, "경기도 수원시"),
                new StationEntity("CHEONGJU",     "청주",   36.6398, 127.4412, "충청북도 청주시"),
                new StationEntity("DAEJEON",      "대전",   36.3504, 127.3845, "대전광역시 유성구"),
                new StationEntity("ANDONG",       "안동",   36.5726, 128.7051, "경상북도 안동시"),
                new StationEntity("POHANG",       "포항",   36.0322, 129.3804, "경상북도 포항시"),
                new StationEntity("DAEGU",        "대구",   35.8714, 128.6014, "대구광역시 중구"),
                new StationEntity("JEONJU",       "전주",   35.8242, 127.1480, "전라북도 전주시"),
                new StationEntity("ULSAN",        "울산",   35.5581, 129.3239, "울산광역시 중구"),
                new StationEntity("CHANGWON",     "창원",   35.1679, 128.5667, "경상남도 창원시"),
                new StationEntity("GWANGJU",      "광주",   35.1595, 126.8526, "광주광역시 북구"),
                new StationEntity("BUSAN",        "부산",   35.1796, 129.0756, "부산광역시 동래구"),
                new StationEntity("YEOSU",        "여수",   34.7394, 127.7401, "전라남도 여수시"),
                new StationEntity("JINJU",        "진주",   35.1600, 128.0406, "경상남도 진주시"),
                new StationEntity("JEJU",         "제주",   33.4996, 126.5312, "제주특별자치도 제주시"),
                new StationEntity("SEOGWIPO",     "서귀포", 33.2462, 126.5069, "제주특별자치도 서귀포시")
        );

        int inserted = 0;
        for (StationEntity station : stations) {
            if (!stationRepo.existsById(station.getStationCode())) {
                stationRepo.save(station);
                inserted++;
            }
        }
        log.info("[DataInitializer] 관측소 " + inserted + "건 INSERT (총 " + stations.size() + "건)");
    }

    // ── 평년 기준일 초기 데이터 ───────────────────────────────────

    private void initNormalYears() {
        List<NormalYearEntity> normalYears = List.of(
                // 벚꽃 (왕벚나무 개화 평년일)
                new NormalYearEntity("SEOGWIPO",     PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 20)),
                new NormalYearEntity("JEJU",         PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 23)),
                new NormalYearEntity("BUSAN",        PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 28)),
                new NormalYearEntity("CHANGWON",     PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 28)),
                new NormalYearEntity("YEOSU",        PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 28)),
                new NormalYearEntity("JINJU",        PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 29)),
                new NormalYearEntity("DAEGU",        PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 29)),
                new NormalYearEntity("GWANGJU",      PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 29)),
                new NormalYearEntity("POHANG",       PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 30)),
                new NormalYearEntity("ULSAN",        PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 3, 31)),
                new NormalYearEntity("JEONJU",       PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  1)),
                new NormalYearEntity("DAEJEON",      PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  1)),
                new NormalYearEntity("CHEONGJU",     PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  2)),
                new NormalYearEntity("ANDONG",       PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  3)),
                new NormalYearEntity("INCHEON",      PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  5)),
                new NormalYearEntity("SUWON",        PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  5)),
                new NormalYearEntity("SEOUL",        PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  6)),
                new NormalYearEntity("HONGCHEON",    PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4,  9)),
                new NormalYearEntity("BUKGANGNEUNG", PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4, 10)),
                new NormalYearEntity("ULLEUNGDO",    PlantType.CHERRY_BLOSSOM, LocalDate.of(2026, 4, 12)),
                // 매화 개화 평년일
                new NormalYearEntity("SEOGWIPO",     PlantType.MAEHWA, LocalDate.of(2026,  1, 20)),
                new NormalYearEntity("JEJU",         PlantType.MAEHWA, LocalDate.of(2026,  2, 14)),
                new NormalYearEntity("BUSAN",        PlantType.MAEHWA, LocalDate.of(2026,  2, 21)),
                new NormalYearEntity("YEOSU",        PlantType.MAEHWA, LocalDate.of(2026,  2, 22)),
                new NormalYearEntity("CHANGWON",     PlantType.MAEHWA, LocalDate.of(2026,  2, 23)),
                new NormalYearEntity("GWANGJU",      PlantType.MAEHWA, LocalDate.of(2026,  2, 24)),
                new NormalYearEntity("JINJU",        PlantType.MAEHWA, LocalDate.of(2026,  2, 25)),
                new NormalYearEntity("DAEGU",        PlantType.MAEHWA, LocalDate.of(2026,  2, 26)),
                new NormalYearEntity("JEONJU",       PlantType.MAEHWA, LocalDate.of(2026,  3,  1)),
                new NormalYearEntity("DAEJEON",      PlantType.MAEHWA, LocalDate.of(2026,  3,  3)),
                new NormalYearEntity("CHEONGJU",     PlantType.MAEHWA, LocalDate.of(2026,  3,  5)),
                new NormalYearEntity("SEOUL",        PlantType.MAEHWA, LocalDate.of(2026,  3,  6)),
                new NormalYearEntity("ANDONG",       PlantType.MAEHWA, LocalDate.of(2026,  3,  8)),
                new NormalYearEntity("INCHEON",      PlantType.MAEHWA, LocalDate.of(2026,  3, 10)),
                new NormalYearEntity("SUWON",        PlantType.MAEHWA, LocalDate.of(2026,  3, 10)),
                new NormalYearEntity("HONGCHEON",    PlantType.MAEHWA, LocalDate.of(2026,  3, 14)),
                new NormalYearEntity("BUKGANGNEUNG", PlantType.MAEHWA, LocalDate.of(2026,  3, 15)),
                // 개나리 개화 평년일
                new NormalYearEntity("SEOGWIPO",     PlantType.FORSYTHIA, LocalDate.of(2026,  2, 14)),
                new NormalYearEntity("JEJU",         PlantType.FORSYTHIA, LocalDate.of(2026,  2, 18)),
                new NormalYearEntity("BUSAN",        PlantType.FORSYTHIA, LocalDate.of(2026,  2, 25)),
                new NormalYearEntity("YEOSU",        PlantType.FORSYTHIA, LocalDate.of(2026,  2, 26)),
                new NormalYearEntity("CHANGWON",     PlantType.FORSYTHIA, LocalDate.of(2026,  2, 26)),
                new NormalYearEntity("JINJU",        PlantType.FORSYTHIA, LocalDate.of(2026,  2, 28)),
                new NormalYearEntity("GWANGJU",      PlantType.FORSYTHIA, LocalDate.of(2026,  2, 27)),
                new NormalYearEntity("DAEGU",        PlantType.FORSYTHIA, LocalDate.of(2026,  3,  1)),
                new NormalYearEntity("ULSAN",        PlantType.FORSYTHIA, LocalDate.of(2026,  3,  3)),
                new NormalYearEntity("JEONJU",       PlantType.FORSYTHIA, LocalDate.of(2026,  3,  3)),
                new NormalYearEntity("POHANG",       PlantType.FORSYTHIA, LocalDate.of(2026,  3,  5)),
                new NormalYearEntity("DAEJEON",      PlantType.FORSYTHIA, LocalDate.of(2026,  3,  5)),
                new NormalYearEntity("CHEONGJU",     PlantType.FORSYTHIA, LocalDate.of(2026,  3,  8)),
                new NormalYearEntity("ANDONG",       PlantType.FORSYTHIA, LocalDate.of(2026,  3, 10)),
                new NormalYearEntity("SUWON",        PlantType.FORSYTHIA, LocalDate.of(2026,  3, 12)),
                new NormalYearEntity("INCHEON",      PlantType.FORSYTHIA, LocalDate.of(2026,  3, 13)),
                new NormalYearEntity("SEOUL",        PlantType.FORSYTHIA, LocalDate.of(2026,  3, 14)),
                new NormalYearEntity("ULLEUNGDO",    PlantType.FORSYTHIA, LocalDate.of(2026,  3, 20)),
                new NormalYearEntity("HONGCHEON",    PlantType.FORSYTHIA, LocalDate.of(2026,  3, 20)),
                new NormalYearEntity("BUKGANGNEUNG", PlantType.FORSYTHIA, LocalDate.of(2026,  3, 22)),
                // 진달래 개화 평년일
                new NormalYearEntity("SEOGWIPO",     PlantType.AZALEA, LocalDate.of(2026,  2, 20)),
                new NormalYearEntity("JEJU",         PlantType.AZALEA, LocalDate.of(2026,  2, 25)),
                new NormalYearEntity("BUSAN",        PlantType.AZALEA, LocalDate.of(2026,  3,  3)),
                new NormalYearEntity("YEOSU",        PlantType.AZALEA, LocalDate.of(2026,  3,  5)),
                new NormalYearEntity("CHANGWON",     PlantType.AZALEA, LocalDate.of(2026,  3,  4)),
                new NormalYearEntity("JINJU",        PlantType.AZALEA, LocalDate.of(2026,  3,  5)),
                new NormalYearEntity("GWANGJU",      PlantType.AZALEA, LocalDate.of(2026,  3,  5)),
                new NormalYearEntity("DAEGU",        PlantType.AZALEA, LocalDate.of(2026,  3,  7)),
                new NormalYearEntity("ULSAN",        PlantType.AZALEA, LocalDate.of(2026,  3,  8)),
                new NormalYearEntity("JEONJU",       PlantType.AZALEA, LocalDate.of(2026,  3,  9)),
                new NormalYearEntity("POHANG",       PlantType.AZALEA, LocalDate.of(2026,  3, 10)),
                new NormalYearEntity("DAEJEON",      PlantType.AZALEA, LocalDate.of(2026,  3, 10)),
                new NormalYearEntity("CHEONGJU",     PlantType.AZALEA, LocalDate.of(2026,  3, 13)),
                new NormalYearEntity("ANDONG",       PlantType.AZALEA, LocalDate.of(2026,  3, 15)),
                new NormalYearEntity("SUWON",        PlantType.AZALEA, LocalDate.of(2026,  3, 17)),
                new NormalYearEntity("INCHEON",      PlantType.AZALEA, LocalDate.of(2026,  3, 18)),
                new NormalYearEntity("SEOUL",        PlantType.AZALEA, LocalDate.of(2026,  3, 19)),
                new NormalYearEntity("ULLEUNGDO",    PlantType.AZALEA, LocalDate.of(2026,  3, 22)),
                new NormalYearEntity("HONGCHEON",    PlantType.AZALEA, LocalDate.of(2026,  3, 25)),
                new NormalYearEntity("BUKGANGNEUNG", PlantType.AZALEA, LocalDate.of(2026,  3, 27)),
                // 단풍 시작 평년일
                new NormalYearEntity("BUKGANGNEUNG", PlantType.MAPLE, LocalDate.of(2026, 10, 20)),
                new NormalYearEntity("HONGCHEON",    PlantType.MAPLE, LocalDate.of(2026, 10, 24)),
                new NormalYearEntity("ANDONG",       PlantType.MAPLE, LocalDate.of(2026, 10, 27)),
                new NormalYearEntity("ULLEUNGDO",    PlantType.MAPLE, LocalDate.of(2026, 10, 28)),
                new NormalYearEntity("JEJU",         PlantType.MAPLE, LocalDate.of(2026, 10, 30)),
                new NormalYearEntity("SEOUL",        PlantType.MAPLE, LocalDate.of(2026, 11,  7)),
                new NormalYearEntity("INCHEON",      PlantType.MAPLE, LocalDate.of(2026, 11,  8)),
                new NormalYearEntity("CHEONGJU",     PlantType.MAPLE, LocalDate.of(2026, 11,  3)),
                new NormalYearEntity("DAEJEON",      PlantType.MAPLE, LocalDate.of(2026, 11,  4)),
                new NormalYearEntity("DAEGU",        PlantType.MAPLE, LocalDate.of(2026, 11,  5)),
                new NormalYearEntity("JEONJU",       PlantType.MAPLE, LocalDate.of(2026, 11,  5)),
                new NormalYearEntity("GWANGJU",      PlantType.MAPLE, LocalDate.of(2026, 11,  6)),
                new NormalYearEntity("POHANG",       PlantType.MAPLE, LocalDate.of(2026, 11,  6)),
                new NormalYearEntity("BUSAN",        PlantType.MAPLE, LocalDate.of(2026, 11,  8)),
                new NormalYearEntity("SEOGWIPO",     PlantType.MAPLE, LocalDate.of(2026, 11, 15))
        );

        int inserted = 0;
        for (NormalYearEntity entity : normalYears) {
            if (!normalYearRepo.existsByStationCodeAndPlantType(entity.getStationCode(), entity.getPlantType())) {
                normalYearRepo.save(entity);
                inserted++;
            }
        }
        log.info("[DataInitializer] 평년 기준일 " + inserted + "건 INSERT (총 " + normalYears.size() + "건)");
    }
}
