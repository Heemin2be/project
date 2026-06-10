package seasonal.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import seasonal.domain.MapViewModel;
import seasonal.calculator.FloweringPlantStatusCalculator;
import seasonal.calculator.FoliagePlantStatusCalculator;
import seasonal.calculator.StatusCalculatorResolver;
import seasonal.config.ApiKeyConfig;
import seasonal.config.DefaultIconRuleRepository;
import seasonal.config.KmaApiKeyConfig;
import seasonal.enums.PlantType;
import seasonal.map.GoogleMapsProvider;
import seasonal.map.MapProvider;
import seasonal.repository.CsvNormalYearRepository;
import seasonal.repository.CsvStationRepository;
import seasonal.repository.KmaPhenologyRepository;
import seasonal.service.ComparisonService;
import seasonal.service.JsonSupport;
import seasonal.service.ObservationMapService;
import seasonal.weather.KmaWeatherService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.List;

public class SeasonalPlantWebServer {
    private static final int DEFAULT_PORT = 8080;
    private final ObservationMapService mapService;
    private final ApiKeyConfig apiKeyConfig;
    private final MapProvider mapProvider;

    public SeasonalPlantWebServer() {
        this(
                buildMapService(),
                new ApiKeyConfig(),
                new GoogleMapsProvider()
        );
    }

    private static ObservationMapService buildMapService() {
        String kmaKey = new KmaApiKeyConfig().loadApiKey();

        KmaPhenologyRepository phenologyRepo = new KmaPhenologyRepository(kmaKey);
        KmaWeatherService weatherService = kmaKey.isBlank() ? null : new KmaWeatherService(kmaKey);

        return new ObservationMapService(
                new CsvStationRepository(),
                phenologyRepo,
                new CsvNormalYearRepository(),
                new ComparisonService(),
                new StatusCalculatorResolver(List.of(
                        new FloweringPlantStatusCalculator(),
                        new FoliagePlantStatusCalculator()
                )),
                new DefaultIconRuleRepository(),
                weatherService
        );
    }

    SeasonalPlantWebServer(
            ObservationMapService mapService,
            ApiKeyConfig apiKeyConfig,
            MapProvider mapProvider
    ) {
        this.mapService = mapService;
        this.apiKeyConfig = apiKeyConfig;
        this.mapProvider = mapProvider;
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        new SeasonalPlantWebServer().start(port);
    }

    public HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/plants", exchange -> sendJson(exchange, JsonSupport.plantsJson()));
        server.createContext("/api/map", new MapApiHandler());
        server.createContext("/config.js", this::handleMapConfig);
        server.createContext("/", new StaticResourceHandler());
        server.setExecutor(Executors.newFixedThreadPool(8, runnable -> {
            Thread thread = new Thread(runnable, "seasonal-http-worker");
            thread.setDaemon(true);
            return thread;
        }));
        server.start();
        printStartupStatus(server.getAddress().getPort());
        return server;
    }

    private void printStartupStatus(int port) {
        String googleKey = apiKeyConfig.loadGoogleMapsApiKey();
        String kmaKey    = new KmaApiKeyConfig().loadApiKey();
        String google = googleKey.isBlank() ? "✗ 미설정" : "✓ 로드됨";
        String kma    = kmaKey.isBlank()    ? "✗ 미설정 (CSV 사용)" : "✓ 로드됨";
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│  API 키 상태                              │");
        System.out.printf( "│  Google Maps : %-26s│%n", google);
        System.out.printf( "│  KMA         : %-26s│%n", kma);
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println("Seasonal plant web server started: http://localhost:" + port);
    }

    private void handleMapConfig(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed");
            return;
        }

        String apiKey = apiKeyConfig.loadGoogleMapsApiKey();
        String apiUrl = apiKey.isBlank() ? "" : mapProvider.createJavaScriptApiUrl(apiKey);
        String body = "window.MAP_PROVIDER = " + jsonString(mapProvider.getName()) + ";\n"
                + "window.GOOGLE_MAPS_API_URL = " + jsonString(apiUrl) + ";\n";
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        sendText(exchange, 200, body, "application/javascript; charset=utf-8");
    }

    private final class MapApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method Not Allowed");
                return;
            }

            try {
                Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
                PlantType plantType = PlantType.valueOf(query.getOrDefault("plantType", "CHERRY_BLOSSOM"));
                LocalDate date = LocalDate.parse(query.getOrDefault("date", LocalDate.now().toString()));
                MapViewModel viewModel = mapService.createMapView(plantType, date);
                sendJson(exchange, JsonSupport.mapViewJson(viewModel));
            } catch (RuntimeException exception) {
                sendText(exchange, 400, "{\"error\":\"" + exception.getMessage() + "\"}", "application/json");
            }
        }
    }

    private static final class StaticResourceHandler implements HttpHandler {
        private static final Path VITE_DIST_DIR = Path.of("frontend", "dist");

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method Not Allowed");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            StaticFile staticFile = readViteDistFile(path);
            if (staticFile == null && !path.contains(".")) {
                staticFile = readViteDistFile("/index.html");
            }
            if (staticFile != null) {
                sendStaticFile(exchange, staticFile.path(), staticFile.body());
                return;
            }

            String resourcePath = "/web" + path;
            InputStream inputStream = SeasonalPlantWebServer.class.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                sendText(exchange, 404, "Not Found");
                return;
            }

            byte[] body;
            try (inputStream) {
                body = inputStream.readAllBytes();
            }

            sendStaticFile(exchange, path, body);
        }

        private static StaticFile readViteDistFile(String requestPath) throws IOException {
            String relativePath = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
            Path candidate = VITE_DIST_DIR.resolve(relativePath).normalize();
            if (!candidate.startsWith(VITE_DIST_DIR) || !Files.isRegularFile(candidate)) {
                return null;
            }
            return new StaticFile(requestPath, Files.readAllBytes(candidate));
        }
    }

    private record StaticFile(String path, byte[] body) {
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> values = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return values;
        }

        for (String pair : rawQuery.split("&")) {
            String[] tokens = pair.split("=", 2);
            String key = decode(tokens[0]);
            String value = tokens.length > 1 ? decode(tokens[1]) : "";
            values.put(key, value);
        }
        return values;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String jsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static void sendJson(HttpExchange exchange, String body) throws IOException {
        sendText(exchange, 200, body, "application/json; charset=utf-8");
    }

    private static void sendText(HttpExchange exchange, int status, String body) throws IOException {
        sendText(exchange, status, body, "text/plain; charset=utf-8");
    }

    private static void sendText(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static void sendStaticFile(HttpExchange exchange, String path, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType(path));
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(body);
        }
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (path.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (path.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (path.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (path.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        if (path.endsWith(".png")) {
            return "image/png";
        }
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (path.endsWith(".webp")) {
            return "image/webp";
        }
        if (path.endsWith(".ico")) {
            return "image/x-icon";
        }
        return "application/octet-stream";
    }
}
