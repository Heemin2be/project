# Java 웹 기반 계절별 식물 관측 지도

Java 내장 `HttpServer`와 Vite React로 만든 계절별 식물 관측 지도 프로젝트입니다.

Google Maps는 실제 Maps JavaScript API를 사용하고, 식물 관측값은 현재 샘플 CSV 데이터를 사용합니다.
프론트엔드는 `frontend/`에 분리되어 있으며, 개발 중에는 Vite dev server가 Java API를 프록시합니다.

## 실행

Java API 서버:

```bash
./gradlew run
```

Vite 개발 서버:

```bash
cd frontend
npm install
npm run dev
```

개발 중 브라우저 접속:

```text
http://localhost:5173
```

Vite 빌드 후 Java 서버 단독 실행:

```bash
cd frontend
npm run build
cd ..
./gradlew run
```

빌드된 화면은 Java 서버에서 접속합니다.

```text
http://localhost:8080
```

## Google Maps 키 설정

`config/google-maps.properties.example`을 참고하여
`config/google-maps.properties` 파일을 만듭니다.

```properties
google.maps.apiKey=YOUR_GOOGLE_MAPS_API_KEY
```

또는 `GOOGLE_MAPS_API_KEY` 환경 변수를 사용할 수 있습니다.
실제 키 파일은 `.gitignore`에 포함되어 있습니다.

## 주요 구조

```text
src/main/java/seasonal
├─ calculator
├─ config
├─ domain
├─ enums
├─ map
├─ repository
├─ service
└─ web

src/main/resources
└─ data

frontend
├─ index.html
├─ vite.config.js
└─ src
```

## 최종 문서

`docs/project_progress_update_1_web_final.pdf`

## 설계 규모

- 패키지: 8개
- 클래스: 26개(추상 클래스 1개 포함)
- 인터페이스: 7개
- enum: 5개

## 테스트

```bash
./gradlew test
```

좌표 유효성, 상태 계산 상속 구조, 평년 비교, CSV 로딩, 좌표 투영, HTTP API를 검증합니다.
