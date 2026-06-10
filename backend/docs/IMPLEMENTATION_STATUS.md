# 구현 현황

## 완료

- Java `HttpServer` 기반 웹 서버
- Google Maps JavaScript API 연동
- `AdvancedMarkerElement` 기반 관측지점 마커
- 식물 및 날짜 조회 API
- CSV 기반 관측지점, 관측기록, 평년값 저장
- Repository, Service, enum, 예외 처리
- 추상 상태 계산기와 개화형/단풍형 상속 구조
- 8개 패키지, 26개 클래스, 7개 인터페이스, 5개 enum
- Google Maps 키 외부 설정 및 `.gitignore` 처리
- 단위/API 테스트
- 테스트 클래스 6개, 테스트 케이스 10개 통과

## 데이터 범위

- 지도: 실제 Google Maps API
- 식물 관측값: 과제용 샘플 CSV
- 실시간 공공데이터 API: 미연동

## 실행 및 검증

```bash
./gradlew test
./gradlew run
```

브라우저:

```text
http://localhost:8080
```

## 제출 전 사용자 확인

- Google Cloud HTTP referrer 제한: `http://localhost:8080/*`
- Maps JavaScript API만 사용하도록 API 제한
- Google Cloud 결제 예산 알림 설정
- 최종 PDF와 프로젝트 폴더 압축
