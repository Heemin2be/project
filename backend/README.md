# 계절별 식물 관측 지도

Spring Boot 3.4.1 백엔드 + Vite React 프론트엔드로 구성된 계절별 식물 관측 지도 프로젝트입니다.

식물 관측 데이터는 기상청 생물계절관측 API(KMA)를 통해 실시간으로 조회하며, Google Maps JavaScript API로 지도에 표시합니다. 백엔드와 프론트엔드는 완전히 분리되어 있습니다.

## 실행

Spring Boot API 서버 (`backend/`):

```bash
./gradlew bootRun
```

Vite 개발 서버 (`frontend/`):

```bash
cd frontend
npm install
npm run dev
```

개발 중 브라우저 접속:

```text
http://localhost:5173
```

프로덕션 빌드:

```bash
cd frontend
npm run build
```

빌드 결과는 `frontend/dist/`에 생성됩니다. 백엔드는 정적 파일을 서빙하지 않으므로 별도 웹 서버 또는 Vite preview를 사용하세요.

```bash
cd frontend
npm run preview   # http://localhost:4173
```

## 환경 변수 설정

### 백엔드 (`backend/.env`)

`backend/.env.example`을 참고하여 `backend/.env`를 만듭니다.

```env
KMA_WEATHER_API_KEY=YOUR_KMA_API_KEY
```

기상청 공공데이터포털에서 발급받은 서비스 키(디코딩된 값)를 사용합니다.

### 프론트엔드 (`frontend/.env`)

`frontend/.env.example`을 참고하여 `frontend/.env`를 만듭니다.

```env
VITE_GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

두 키 파일 모두 `.gitignore`에 포함되어 있습니다.

## 주요 구조

```text
backend/
└─ src/main/java/seasonal
   ├─ calculator      # 개화/단풍 상태 계산기 (추상 클래스 + 상속)
   ├─ config          # KMA API 키, 아이콘 규칙
   ├─ domain          # 도메인 모델 (ObservationStation, ObservationRecord 등)
   ├─ enums           # PlantType, PhenologyStage, ComparisonResultType 등
   ├─ repository      # KmaPhenologyRepository, HardcodedStationRepository 등
   ├─ service         # ObservationMapService, ComparisonService
   ├─ weather         # KmaWeatherService (초단기실황 기온)
   └─ web             # SeasonalController, CorsConfig, GlobalExceptionHandler, dto

frontend/
├─ index.html
├─ vite.config.js
└─ src
   ├─ main.jsx        # React 앱 전체
   └─ styles.css
```

## API 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/plants` | 지원 식물 목록 |
| GET | `/api/map?plantType=&date=` | 관측 지도 데이터 |

## 테스트

```bash
./gradlew test
```

좌표 유효성, 상태 계산 상속 구조, 평년 비교, HTTP API를 검증합니다.
