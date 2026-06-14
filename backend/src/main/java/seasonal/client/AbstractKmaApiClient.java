package seasonal.client;

import seasonal.config.KmaApiKeyConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 기상청(KMA) Open API 클라이언트 추상 클래스.
 *
 * <p>공통 관심사(API 키 로딩, HttpClient 생성, HTTP GET 요청)를 캡슐화하여
 * 하위 클래스가 비즈니스 로직에만 집중할 수 있도록 합니다.</p>
 *
 * <pre>
 * AbstractKmaApiClient  ← 이 클래스 (상속의 기반)
 *   ├── KmaPhenologyRepository   (생물계절관측 API)
 *   └── KmaWeatherService        (초단기실황 API)
 * </pre>
 */
public abstract class AbstractKmaApiClient {

    /** KMA API 인증키 (trim된 원문, 인코딩 없음) */
    protected final String apiKey;

    /** 공용 HttpClient (연결 타임아웃 5초) */
    protected final HttpClient httpClient;

    protected AbstractKmaApiClient(KmaApiKeyConfig kmaApiKeyConfig) {
        String key = kmaApiKeyConfig.loadApiKey();
        this.apiKey = (key == null) ? "" : key.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 주어진 URL로 HTTP GET 요청을 보내고 응답 본문을 문자열로 반환합니다.
     *
     * @param url 요청할 완전한 URL (쿼리 파라미터 포함)
     * @return HTTP 응답 (status code + body)
     * @throws Exception 네트워크 오류, 타임아웃 등
     */
    protected HttpResponse<String> get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** API 키가 설정되어 있는지 확인합니다. */
    protected boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
