# The News Humor Project

Spring Boot 3.2.2 기반의 뉴스 포털 스타일 커뮤니티 대시보드입니다.

## 기술 스택
- **Backend:** Spring Boot 3.2.2, Java 21 (Temurin)
- **Frontend:** Thymeleaf, Bootstrap 5.3.2, jQuery 3.7.1
- **Styling:** Custom CSS (NYT Style Typography & Board Layout)

## 주요 디자인 특징
1. **New York Times Style:**
   - Playfair Display / Noto Serif KR 폰트 사용.
   - 굵은 로고와 날짜 표시 바.
   - 정갈한 보더와 여백.
2. **Today's Humor Layout:**
   - 중앙 게시판 리스트 구조 (번호, 분류, 제목, 이름, 날짜, 조회, 추천).
   - 직관적인 커뮤니티형 UI.

## 실행 및 빌드
- **서버 실행:** `./mvnw spring-boot:run` (또는 인텔리제이에서 DemoApplication 실행)
- **빌드:** `./mvnw clean package`

## 프로젝트 구조
- `src/main/java/com/example/demo/DemoApplication.java`: 메인 엔트리 포인트.
- `src/main/java/com/example/demo/HomeController.java`: 메인 페이지 및 샘플 데이터 컨트롤러.
- `src/main/resources/templates/index.html`: 메인 타임리프 템플릿.
- `src/main/resources/static/css/style.css`: NYT 스타일 정의 CSS.
