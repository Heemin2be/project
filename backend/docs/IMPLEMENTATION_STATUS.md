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
- 실시간 공공데이터 API: 연동

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
- Google Cloud 결제 예산 알림 설정

# 수정(write by dohoon)
- 지금 HTTP 서버로 되어있는거 스프링 부트로 변경, 프론트는 리액트로 변경
- 필요 라이브러리는 요청할 
- 기본값 CSV 안쓸거임(요청실패 시 오류를 띄울것) 필요한 API 있으면 요청할것
- 설계문서 작성 필요함 구현 끝나면 역공학하여 MD문서로 정리할것
- 기존에 필요한 API는 연동해놓은 상태