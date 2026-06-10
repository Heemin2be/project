package seasonal.web;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeasonalPlantWebServerTest {
    private HttpServer server;
    private String baseUrl;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void startServer() throws Exception {
        server = new SeasonalPlantWebServer().start(0);
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void returnsPlantsAndMapJson() throws Exception {
        HttpResponse<String> plants = get("/api/plants");
        HttpResponse<String> map = get("/api/map?plantType=CHERRY_BLOSSOM&date=2026-04-06");

        assertEquals(200, plants.statusCode());
        assertTrue(plants.body().contains("CHERRY_BLOSSOM"));
        assertEquals(200, map.statusCode());
        assertTrue(map.body().contains("\"markers\""));
        assertTrue(map.body().contains("\"stationName\":\"서울\""));
    }

    @Test
    void returnsBadRequestForInvalidDate() throws Exception {
        HttpResponse<String> response = get("/api/map?plantType=CHERRY_BLOSSOM&date=invalid");

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("error"));
    }

    @Test
    void returnsEmptyMarkersBeforeObservationPeriod() throws Exception {
        HttpResponse<String> response = get("/api/map?plantType=CHERRY_BLOSSOM&date=2025-01-01");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"markers\":[]"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
