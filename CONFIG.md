# Chat Service 설정 컨벤션

Discovery / Gateway 와 동일한 YAML 프로필 구조를 사용합니다.

## YAML 파일 구조

| 파일 | 역할 |
|------|------|
| `application.yml` | 모든 환경 공통 설정 (앱명, Eureka, springdoc, codec) |
| `application-local.yml` | 로컬 개발 (팀 공통, Git 포함) |
| `application-dev.yml` | 통합/개발 서버 |
| `application-prod.yml` | 운영 서버 |

## 프로필 활성화

```yaml
spring.profiles.active: ${SPRING_PROFILES_ACTIVE:local}
```

- 개인 PC: 기본 `local`
- 통합 검증 노트북: `SPRING_PROFILES_ACTIVE=dev`
- 배포: `SPRING_PROFILES_ACTIVE=prod`

## 포트 및 Eureka

- `server.port: 0` — OS가 할당하는 사용 가능한 랜덤 포트
- Eureka instance-id: `서비스명:실제포트` (예: `chat-service:51234`) — 기동 로그와 Eureka 대시보드에서 확인
- `eureka.instance.prefer-ip-address: true`
- Eureka 등록명: `chat-service`

## Secret 관리 규칙

**YAML 파일에 MongoDB 계정/비밀번호, URI를 직접 작성하지 않습니다.**

| 항목 | local | dev / prod |
|------|-------|------------|
| MongoDB URI | `.env` | 배포 환경변수 |
| Eureka URL (local) | `application-local.yml` (`localhost:8761`) | `${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}` |

### 로컬 실행 준비

```bash
cp .env.example .env
# .env 파일에 실제 MongoDB URI 입력
```

개인별 override가 필요하면 Git에 포함되지 않는 `application-local-secret.yml` 을 추가할 수 있습니다.

## 로컬 실행

```bash
# 1. Discovery 실행
cd ../discovery && ./gradlew bootRun

# 2. .env 설정 후 Chat Service 실행
cd ../chat-service && ./gradlew bootRun
```

- `bootRun` 은 프로젝트 루트의 `.env` 를 자동 로드합니다.
- IDE에서 실행할 때는 **Run Configuration** `ChatServiceApplication` 을 사용하거나, Working directory 가 프로젝트 루트인지 확인하세요.
- `application-local.yml` 이 `optional:file:.env[.properties]` 를 import 하므로 IDE main 실행 시에도 `.env` 가 로드됩니다.

Eureka Dashboard (`http://localhost:8761`) 에서 `CHAT-SERVICE` 등록을 확인합니다.

## 필수 환경변수 (local / dev / prod)

| 변수 | 설명 |
|------|------|
| `SPRING_MONGODB_URI` | MongoDB connection URI (replica set 포함) |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | dev/prod Eureka URL (local은 YAML 고정) |

### local MongoDB URI 예시

```
mongodb://admin:your-password-here@localhost:27017,localhost:27018,localhost:27019/chatting_db?authSource=admin&replicaSet=rs0
```

## Gateway 연동

Gateway `application-local.yml` 에 `chat-service` Swagger URL 이 등록되어 있습니다.

- API Docs (via Gateway): `/chat-service/v3/api-docs`
- Discovery Locator: `/chat-service/**`
