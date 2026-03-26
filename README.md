# DurumiBridge

두루미마을 마인크래프트 서버와 커뮤니티 웹사이트를 연결하는 Paper 플러그인입니다.

## 기능

- **서버 상태 동기화** — 온라인 여부, 접속자, TPS 등을 30초마다 웹사이트에 푸시
- **플레이어 위치 추적** — 접속 중인 플레이어의 좌표, 체력, 월드 정보 전송
- **맵 타일 렌더링** — 월드를 256x256 타일로 렌더링하여 웹사이트 지도에 표시
- **타일 자동 업로드** — 렌더링 완료 후 Vercel Blob Storage에 자동 업로드
- **REST API 서버** — 내장 HTTP 서버로 공지사항 등 관리 API 제공

## 요구 사항

- Paper 1.21.11+
- Java 21

## 설치

1. [Releases](https://github.com/kimseuseu/DurumiBridge/releases)에서 `DurumiBridge-x.x.x.jar` 다운로드
2. 서버 `plugins/` 폴더에 복사
3. 서버 시작 후 `plugins/DurumiBridge/config.yml` 설정

## 설정 (config.yml)

```yaml
# HTTP API 서버
api:
  port: 8080
  api-key: "보안키를-변경하세요"

# 맵 렌더링
map:
  render-interval: 180  # 분 단위 (기본 3시간)
  render-radius: 50     # 스폰 기준 청크 반경
  worlds:
    - "world"

# 웹사이트 동기화 (Push 방식)
sync:
  enabled: true
  url: "https://your-site.vercel.app/api/sync"
  secret: "동기화-시크릿-키"
  interval: 30  # 초 단위
```

## 명령어

| 명령어 | 설명 | 권한 |
|---|---|---|
| `/durumi reload` | 설정 리로드 | `durumi.admin` (OP) |
| `/durumi announce` | 공지 관리 | `durumi.admin` (OP) |
| `/durumi verify` | 계정 인증 | `durumi.verify` (모두) |

## 빌드

```bash
# Java 21 필요
./gradlew jar
# 결과: build/libs/DurumiBridge-x.x.x.jar
```

## 아키텍처

```
[MC 서버] --push(30s)--> [웹사이트 /api/sync]
[MC 서버] --render(3h)--> [타일 생성] --upload--> [Vercel Blob Storage]
```

포트포워딩 없이 동작하는 **Push 기반** 구조입니다. 플러그인이 주기적으로 웹사이트 API에 데이터를 전송합니다.

## 라이선스

MIT

## 제작

moo_gi — [두루미마을](https://durumitown.kr)
