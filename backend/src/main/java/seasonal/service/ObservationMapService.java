package seasonal.service;

import org.springframework.stereotype.Service;
import seasonal.domain.MapMarker;
import seasonal.domain.MapViewModel;
import seasonal.domain.NormalYearReference;
import seasonal.domain.ObservationRecord;
import seasonal.domain.ObservationStation;
import seasonal.calculator.PhenologyStatusCalculator;
import seasonal.calculator.StatusCalculatorResolver;
import seasonal.config.IconRuleRepository;
import seasonal.enums.ComparisonResultType;
import seasonal.enums.PhenologyStage;
import seasonal.enums.PlantType;
import seasonal.repository.NormalYearRepository;
import seasonal.repository.ObservationRepository;
import seasonal.repository.StationRepository;
import seasonal.weather.KmaWeatherService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ObservationMapService {
    private final StationRepository stationRepository;
    private final ObservationRepository observationRepository;
    private final NormalYearRepository normalYearRepository;
    private final ComparisonService comparisonService;
    private final StatusCalculatorResolver calculatorResolver;
    private final IconRuleRepository iconRuleRepository;
    private final KmaWeatherService weatherService;

    public ObservationMapService(
            StationRepository stationRepository,
            ObservationRepository observationRepository,
            NormalYearRepository normalYearRepository,
            ComparisonService comparisonService,
            StatusCalculatorResolver calculatorResolver,
            IconRuleRepository iconRuleRepository,
            KmaWeatherService weatherService
    ) {
        this.stationRepository = stationRepository;
        this.observationRepository = observationRepository;
        this.normalYearRepository = normalYearRepository;
        this.comparisonService = comparisonService;
        this.calculatorResolver = calculatorResolver;
        this.iconRuleRepository = iconRuleRepository;
        this.weatherService = weatherService;
    }

    public MapViewModel createMapView(PlantType plantType, LocalDate queryDate) {
        Map<String, ObservationStation> stations = stationRepository.findAll().stream()
                .collect(Collectors.toMap(ObservationStation::getStationCode, Function.identity()));

        Map<String, NormalYearReference> normalYears = normalYearRepository.findByPlantType(plantType).stream()
                .collect(Collectors.toMap(NormalYearReference::getStationCode, Function.identity()));

        // 기온 데이터 조회 (best-effort: 실패해도 빈 Map 반환)
        Map<String, Double> temperatures = weatherService.fetchTemperatures(List.copyOf(stations.values()));

        List<MapMarker> markers = observationRepository.findByPlantType(plantType).stream()
                .filter(record -> !record.getObservedDate().isAfter(queryDate))
                .collect(Collectors.groupingBy(ObservationRecord::getStationCode))
                .entrySet()
                .stream()
                .map(entry -> latestRecordMarker(entry.getValue(), stations, normalYears, queryDate, temperatures))
                .sorted(Comparator.comparing(marker -> marker.getStation().getName()))
                .toList();

        return new MapViewModel(plantType, queryDate, markers);
    }

    private MapMarker latestRecordMarker(
            List<ObservationRecord> records,
            Map<String, ObservationStation> stations,
            Map<String, NormalYearReference> normalYears,
            LocalDate queryDate,
            Map<String, Double> temperatures
    ) {
        ObservationRecord latest = records.stream()
                .max(Comparator.comparing(ObservationRecord::getObservedDate))
                .orElseThrow();
        ObservationStation station = stations.get(latest.getStationCode());
        if (station == null) {
            throw new IllegalStateException("관측지점 정보를 찾을 수 없습니다: " + latest.getStationCode());
        }
        NormalYearReference normalYear = normalYears.get(latest.getStationCode());
        ComparisonResultType comparison = normalYear == null
                ? ComparisonResultType.UNKNOWN
                : comparisonService.compare(latest.getObservedDate(), normalYear.getNormalDate());
        PhenologyStatusCalculator calculator = calculatorResolver.resolve(latest.getPlantType());
        PhenologyStage stage = calculator.calculate(queryDate, latest);

        return new MapMarker(
                station,
                latest.getPlantType(),
                stage,
                comparison,
                latest.getSourceType(),
                latest.getObservedDate(),
                iconRuleRepository.findColor(latest.getPlantType(), stage),
                temperatures.get(station.getStationCode())
        );
    }
}
