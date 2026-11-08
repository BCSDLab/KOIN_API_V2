# KOIN prod OpenBao 적용 준비 — 실제 반영 금지

이 PR은 검토용 명세와 최소권한 정책 후보를 추가한다. prod 서버·OpenBao·AWS 설정을 변경하지 않으며 현재 main 배포 workflow와 stage 실행기의 guard를 유지한다. `manifest.json`은 `environment=prod`, `mode=observe`다. 현재 생성기·sanitizer·launcher는 stage 전용이므로 이 명세로 prod 실행 파일을 생성하거나 실행할 수 없다. 완료된 prod 전환으로 간주하지 않는다.

## 환경과 서비스별 소유 경계

| 경로 | 용도 | 적용 대상 |
| --- | --- | --- |
| `kv/prod/koin-api/database` | MySQL·Redis·MongoDB 연결 비밀 | KOIN prod API |
| `kv/prod/koin-api/auth` | JWT·CSRF·만료 설정 | KOIN prod API |
| `kv/prod/koin-api/firebase` | Firebase 서비스 계정 JSON | KOIN prod API 외부 리소스 |
| `kv/prod/koin-api/payment` | Toss 결제 키 | KOIN prod API |
| `kv/prod/koin-api/integrations` | SES·SMS·주소·AI 연동 키 | KOIN prod API |

각 key·Spring target·용도는 manifest에 기록했다. stage의21키 구조를 후보로 가져왔으며 prod 실제 설정과의 완전한 대응은 아직 확인 전이다. 값을 stage에서 복사하거나 KV 버전을 임의로1로 가정하지 않는다. 일반 설정(DATASOURCE_URL, API base URL, profile 등)은 검토된 비밀 없는 allowlist로 분리한다. 프론트엔드에는 공개 설정만 전달한다.

## 적용 순서와 담당 확인

1. **읽기 전용 사전 조사:** prod 인스턴스 ID/주소·SSH/SSM 경로·JVM/메모리·nginx/ALB·현재 배포 스크립트·DB migration 동작을 확인한다. 현재 stage 호스트/8081/ubuntu를 prod 값으로 가정하지 않는다. 실행중인 main artifact와 manifest의 필수 키 대응표를 값 없이 작성한다.
2. **복구·변경 창:** 정상 JAR·현재 설정·upstream·DB migration 영향과 복구 담당자를 기록한다. 원본 자료는 root 전용으로 보존한다. JVM 병렬 실행 가능 여부를 측정해 blue/green 또는 단일 JVM 교체와 중단 시간을 결정한다.
3. **전용 인증과 네트워크:** prod 전용 인증서/역할 `koin-api-prod-read` 및 이 폴더의 정확한5경로 read ACL만 설치한다. prod 호스트만 사설 TLS에 허용하고 stage/prod 서로의 경로와 타 서비스 경로를 읽지 못하는지 검사한다. 공용 IAM 역할 확대·공개8200·CI에 OpenBao 관리자 토큰 전달은 하지 않는다.
4. **KV 이관:** 실제 prod 값을 보호된 경로에서 가져와 저장한다. 필수 키·타입·값 동일성을 비출력 방식으로 검증하고 실제 KV 버전·manifest revision을 읽어 release-lock을 만든다. JWT/DB/API key 교체는 이관과 별도 작업이다.
5. **host 도구 확장 PR:** bcsdpdevops의 stage-only runtime/launch/sanitize/deploy/observe를 환경 명세로 확장하되 prod 인스턴스 ID·profile·경로·포트·인증 역할을 고정 검증한다. 매 기동 fresh 인증, 환경변수 대신 `/run` configtree/외부 JSON, ubuntu 등 실제 앱 UID, 메모리 한도, fail-closed, pending/rollback/hash 검증을 유지한다. 인증서 만료14/7/3일 감시와 갱신/폐기, 격리 복원 시험이 필요하다.
6. **main CD 전환 PR:** 호스트 준비와 비운영 리허설 후 아래 workflow 변경을 별도로 검토한다. workflow_dispatch를 실행하거나 main으로 merge하기 전 prod 반영 승인을 받는다.
7. **실제 적용 승인 후:** 정상 배포·주입 receipt·readiness·주요 조회·허용된 로그인/결제 sandbox/메일 시험을 확인한다. startup/readiness/필수 업무 실패 시 이전 JAR와 설정 버전으로 복구하고 CI는 실패로 종료한다. DB migration은 JAR rollback으로 되돌아가지 않는다.
8. **안정화 후:** 관측기간과 다음 배포까지 정상 확인 후 평문 env/구 JAR/불필요한 GitHub secret을 소비자별로 제거한다. 새 키 교체와 이력·백업 보존 정책은 별도 검토한다.

## 예정된 main workflow 변경 (이번 PR에는 미적용)

- `FCM_ADMIN_SDK_JSON_MAIN`을 JAR에 쓰는 단계 제거.
- 검증된 prod sanitizer로 Firebase 원문 및 비밀 placeholder 제거. class/library 동일성 확인.
- 현재 `SOURCE_JAR_PATH`, `MAIN_SERVER_JAR_PATH`, `MAIN_DEPLOY_SCRIPT_PATH`의 실제 값을 확인한 뒤 명시된 안전 경로로 고정.
- `koin-prod-deploy` concurrency, `cancel-in-progress: false`, timeout 및 서버 lock으로 배포 직렬화.
- 승인된 prod host wrapper만 호출. stage 전용 명령 또는 프로파일 사용 거부.
- 적용 버전/시각/상태만 DevOps에 표시. 로그·CI artifact·PR에 값/개인키/token 출력 금지.

## 검증과 남은 조건

로컬 `python3 deployment/secrets/prod/check.py`는 prod 경로·observe 모드·정확한 read ACL과 현재 생성기의 prod 실행 거부를 확인한다. 네트워크나 실제 비밀에 접근하지 않는다.

배포 승격 조건은 prod 실측·5경로 이관·인증/격리 시험·host 실행기 확장·현재 main artifact 검증·무중단 가능성/점검시간·rollback 리허설·담당자 승인이다. 아직 이 조건들은 완료되지 않았다. SSH 인원 이전·IDE 시험은 보류한다.
