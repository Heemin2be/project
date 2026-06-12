# 구현 현황

## 완료

### 백엔드 (Spring Boot 3.4.1)

- `@SpringBootApplication` 진입점 (`SeasonalApplication`)
- `@RestController` API 엔드포인트 통합 (`/api/plants`, `/api/map`)
- Vite dev server(5173/4173) CORS 허용 (`CorsConfig`)
- 400/500 JSON 오류 응답 (`GlobalExceptionHandler`)
- record DTO (`PlantResponse`, `MarkerResponse`, `MapViewResponse`)
- 서비스/레포지토리/설정 클래스 Spring 어노테이션 적용
- 추상 상태 계산기와 개화형/단풍형 상속 구조
- KMA 생물계절관측 API 연동 (`KmaPhenologyRepository`) — API 실패 시 예외 throw
- KMA 초단기실황 기온 API 연동 (`KmaWeatherService`, 15분 캐싱)
- API 키 `.env` 파일 및 환경변수로 외부 설정 (`KmaApiKeyConfig`)
- 단위/API 테스트 (테스트 클래스 6개, 테스트 케이스 10개)

### 프론트엔드 (React + Vite)

- Google Maps JavaScript API (`AdvancedMarkerElement`) 연동
- Google Maps 키 `VITE_GOOGLE_MAPS_API_KEY` 환경변수로 분리
- 식물 선택 / 날짜 조회 / 관측 지점 목록 UI
- `frontend/.env.example` 제공

### 제거된 항목

- CSV 기반 관측 데이터 (`CsvObservationRepository`, `CsvReader`, `DataLoadException`)
- 백엔드 내장 UI (`resources/web/`, `/config.js` 엔드포인트)
- 지도 투영 패키지 (`seasonal/map/`)
- Google Maps API 키 백엔드 설정 (`ApiKeyConfig`)

## 데이터 소스

| 데이터 | 소스 |
|--------|------|
| 식물 관측 기록 | 기상청 생물계절관측 API (PhnlgObsSvc) |
| 현재 기온 | 기상청 초단기실황 API (VilageFcstInfoService_2.0) |
| 관측소 좌표 / 평년값 | 하드코딩 (7개 관측소) |
| 지도 | Google Maps JavaScript API |

## 실행 및 검증

```bash
./gradlew test
./gradlew bootRun
```

프론트엔드:

```bash
cd frontend && npm run dev   # http://localhost:5173
```

## 남은 작업

- 구현 완료 후 역공학하여 설계 MD 문서 작성