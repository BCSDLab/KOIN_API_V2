# 웹 HttpOnly 쿠키 인증

기존 `/v2/users/login`, `/user/login`, `/user/refresh`, `/user/logout`의 앱용 계약은 유지한다.
웹(PC·모바일 브라우저)은 아래 API로 전환한다. User-Agent의 PC/Mobile 분류로 인증 방식을 선택하지 않는다.
이번 변경은 일반·학생·총학생회 사용자의 웹 인증을 대상으로 하며 사장님·영양사·관리자 로그인은 기존 API를 사용한다.

## API 계약

| 요청 | 입력 | 성공 응답 |
| --- | --- | --- |
| `POST /v2/web/auth/login` | JSON `login_id`, `login_pw`, `auto_login` | 201, 인증 쿠키 2개와 CSRF 일반 쿠키, `user_type`·`csrf_token` |
| `GET /v2/web/auth/csrf` | refresh 쿠키 | 200, CSRF 일반 쿠키 복구, `csrf_token` |
| `POST /v2/web/auth/refresh` | refresh 쿠키, `X-CSRF-Token` | 201, 교체된 인증 쿠키 2개와 CSRF 일반 쿠키, `user_type`·`csrf_token` |
| `POST /v2/web/auth/logout` | refresh 쿠키, `X-CSRF-Token` | 204, 현재 웹 세션 폐기 및 쿠키 삭제 |

웹 인증 API는 허용된 `Origin` 또는 `Referer`가 필요하다. 로그인 본문은 `application/json`만 받는다.
`login_pw`는 기존 로그인과 동일하게 SHA-256 처리한 비밀번호를 전달한다.
`auto_login`은 생략하면 false이며, false일 때는 세 쿠키 모두 브라우저 세션 쿠키다.
브라우저의 세션 복원 기능 때문에 브라우저 종료가 서버 세션의 즉시 폐기를 의미하지는 않는다.

access·refresh 값은 JSON 응답에 포함하지 않는다. CSRF 토큰은 32바이트 난수와 현재 로그인 세션에 연결한
HMAC-SHA256 서명으로 구성되며 세션 ID는 CSRF 토큰에 노출하지 않는다. 서버는 헤더로 받은 토큰의 서명과
현재 Redis 세션의 토큰을 함께 검증한다. 쿠키에 담긴 값만으로 인증하거나 쿠키와 헤더의 단순 일치만 확인하지 않는다.
웹 코드는 CSRF 일반 쿠키를 읽어 쿠키 인증 POST/PUT/PATCH/DELETE 요청의 `X-CSRF-Token` 헤더에 넣는다.
CSRF 토큰을 별도 state나 localStorage에 저장할 필요는 없다. 기존 JSON의 `csrf_token`은 호환성을 위해 유지한다.
CSRF 쿠키가 없으면 `/csrf`를 호출해 복구한다. 단순 새로고침마다 이 API를 호출할 필요는 없다.
로그인 시에는 기존 세션이 없으므로
허용 출처 검사와 JSON Content-Type 제한으로 로그인 CSRF를 방어한다.

## 웹 연동 순서

1. 모든 웹 API 요청에 `credentials: 'include'` 또는 Axios의 `withCredentials: true`를 설정한다.
2. 로그인 성공 시 화면에 필요한 회원 정보를 반영한다. CSRF 토큰은 요청 시 일반 쿠키에서 읽는다.
3. 기존 `document.cookie`, localStorage, Zustand의 access·refresh 저장 및 Bearer 헤더 주입을 제거한다.
4. 쿠키 인증의 상태 변경 요청에는 CSRF 헤더를 추가한다. 조회 요청은 CSRF 헤더가 필요하지 않지만 허용된 출처여야 한다.
5. access 만료 시 `/refresh`를 한 번 호출한 뒤 원래 요청을 재시도한다.
6. 로그아웃은 서버 `/logout`의 성공을 확인한 뒤 화면의 로그인 상태를 비운다.

로그인·재발급·로그아웃·CSRF 조회는 만료된 access 쿠키의 영향을 받지 않는다.
일반 기능 API에서는 명시적인 `Authorization` 헤더가 있으면 기존 헤더 인증만 사용한다.
잘못된 헤더를 쿠키 인증으로 대체하지 않으므로, 웹 전환 시 예전 헤더 주입 코드도 제거해야 한다.
기존 앱 토큰을 웹 쿠키로 넣거나 웹 토큰을 기존 앱 인증 경로로 전달하는 것은 허용하지 않는다.
일반 API의 쿠키 인증은 기존 `@Auth`·`@UserId` 파라미터가 있는 메서드에 적용한다.
회원가입·비밀번호 재설정 등 비인증 API는 남아 있는 쿠키 때문에 인증이나 CSRF 헤더를 요구하지 않는다.

쿠키로 인증하는 일반 API도 GET/HEAD를 포함해 허용된 `Origin` 또는 `Referer`가 필요하다.
기존 채팅 조회 API에는 읽음 상태 변경이 포함되어 있어, 외부 링크 이동만으로 해당 동작이 실행되지 않도록 출처를 확인한다.
브라우저의 정상 fetch/XHR은 Origin 또는 Referer를 전송하지만, `no-referrer` 정책을 적용한 동일 출처 GET이나
API 주소 직접 입력은 403이 될 수 있다. SSR/프록시도 모든 쿠키 인증 요청에 설정된 웹 Origin을 전달해야 한다.
이 조건은 기존 앱 Bearer 요청이나 쿠키 없는 공개 조회에는 적용하지 않는다.

`/user/check/login`처럼 토큰을 query로 받는 기존 전용 API는 쿠키 인증으로 전환하지 않는다.
웹의 로그인 상태 확인은 쿠키와 함께 `/user/auth`를 사용한다.

## 쿠키와 만료 설정

| 항목 | 운영(`prod`) 기본값 |
| --- | --- |
| access 쿠키 | `__Secure-koin-web-access`, Domain `koreatech.in`, Path `/`, 기본 15분 |
| refresh 쿠키 | `__Secure-koin-web-refresh`, Path `/v2/web/auth`, 기본 90일 |
| CSRF 쿠키 | `__Secure-koin-web-csrf`, Domain `koreatech.in`, Path `/`, HttpOnly=false, refresh와 동일한 만료 시각 |
| 인증 쿠키 속성 | access·refresh 모두 HttpOnly |
| 공통 기본 속성 | Secure, SameSite=Lax. refresh만 Domain 미지정(host-only) |
| 자동 로그인 false | Max-Age 없는 세션 쿠키. 서버의 만료 시간은 그대로 적용 |

`WEB_AUTH_ACCESS_TOKEN_TTL`, `WEB_AUTH_REFRESH_TOKEN_TTL`, `WEB_AUTH_COOKIE_SECURE`,
`WEB_AUTH_COOKIE_SAME_SITE` 환경변수로 설정한다. 기존 앱의 JWT 만료 설정은 바꾸지 않는다.
`WEB_AUTH_CSRF_SECRET_KEY`는 새로 필요한 서버 전용 서명 키다. 충분한 난수로 생성한 32바이트 이상의 문자열을
로컬·stage·운영 실행 환경에 주입한다. 누락되거나 짧으면 서버가 시작되지 않는다. 소스나 클라이언트에 키를 넣지 않는다.
같은 환경의 서버 인스턴스들은 동일한 키를 사용한다. 운영·stage의 `JWT_SECRET_KEY`와 `WEB_AUTH_CSRF_SECRET_KEY`는
각 서버에 서로 다른 값으로 주입하고 세션 저장소도 환경별로 구분한다. 변수명이 같아도 키 값을 공유하지 않는다.
실제 배포 서버의 키 값은 이 작업에서 변경하거나 확인하지 않았다. 키를 교체하면 기존 웹 세션은 재로그인이 필요하다.
`local` 프로필에서는 HTTP 개발을 위해 Secure=false와 `koin-web-access`·`koin-web-refresh`·`koin-web-csrf` 이름을 사용한다.
운영에서는 Secure를 유지해야 하며 SameSite=None은 Secure 없이 설정할 수 없다.
`CORS_ALLOWED_ORIGINS`에는 실제 웹의 정확한 origin을 넣는다. `*`나 도메인 접미사 비교로 웹 요청을 허용하지 않는다.

프로필별 기본 공유 범위와 쿠키 이름은 다음과 같다. stage는 프로젝트의 기존 `dev` 프로필을 사용한다.

| 환경 | 프로필 | access·CSRF Domain | 쿠키 이름 접두어 |
| --- | --- | --- | --- |
| 운영 웹·order | `prod` | `koreatech.in` | `koin-web` |
| stage 웹 | `dev` | `stage.koreatech.in` | `koin-stage-web` |
| 로컬·테스트 | `local` / `test` | 미지정 | `koin-web` |

프론트가 읽는 CSRF 쿠키 이름은 운영 `__Secure-koin-web-csrf`, stage `__Secure-koin-stage-web-csrf`다.
SSR에서 전달할 access 쿠키도 각각 `__Secure-koin-web-access`, `__Secure-koin-stage-web-access`를 사용한다.
refresh는 환경과 관계없이 해당 API 호스트와 `/v2/web/auth` 경로에만 보낸다.
두 환경의 쿠키가 함께 전달되어도 각 API는 자기 이름의 쿠키만 읽는다.
쿠키 이름을 바꿔 제출하더라도 서로 다른 JWT·HMAC 서명 키와 해당 환경의 세션으로 검증한다.

`WEB_AUTH_SHARED_COOKIE_DOMAIN`, `WEB_AUTH_COOKIE_NAME_PREFIX`로 프로필 기본값을 덮어쓸 수 있다.
이름을 재정의하더라도 운영·stage의 쿠키 이름 접두어는 서로 다르게 유지한다.
공유 Domain을 명시적으로 빈 값으로 설정하면 host-only로 돌아가며, Secure access·CSRF 이름도 `__Host-`로 바뀐다.
발급·조회·삭제·Swagger에 같은 설정을 적용하므로 프론트는 실제 서버 설정과 동일한 쿠키 이름을 사용해야 한다.

운영의 Domain `koreatech.in`은 stage를 포함한 모든 하위 주소에도 쿠키를 전달한다. 이 범위는 합의한 공유 정책이며,
쿠키 이름·키 분리는 다른 환경에서의 오인증/충돌을 막는 것이지 전송 자체를 막는 것은 아니다.
하위 서버의 Cookie 로그·프록시 처리를 보호해야 하며 HMAC은 유효한 토큰 탈취나 XSS를 해결하지 않는다.
실제 서버 환경변수 변경과 운영·stage 배포는 이 코드 변경에 포함하지 않는다.

## 세션과 동시 요청

웹은 로그인마다 `webAuthSession:<랜덤 세션 ID>`를 생성한다. 앱의 기존 Redis 키와 독립적이며 DB 마이그레이션은 없다.
refresh 원문은 저장하지 않고 SHA-256 해시를 저장한다. 재발급 시 refresh를 교체하되 로그인 시 정한 절대 만료 시간은 연장하지 않는다.
웹 access JWT에도 세션 ID를 넣고 요청마다 Redis 세션을 확인하므로 웹 로그아웃 후 남은 access도 사용할 수 없다.
웹 인증 시 DB에서도 계정의 존재 여부를 확인하므로 `@UserId` API도 탈퇴 계정의 쿠키를 받지 않는다.
이 때문에 쿠키 인증은 Redis와 사용자 DB 가용성에 의존하며, 조회 실패 시 인증을 허용하지 않는다.
Redis 전용 CSRF 조회·로그아웃에는 SQL 트랜잭션을 만들지 않고, access 인증도 Redis 대기 전에 SQL 트랜잭션을 시작하지 않는다.

Redis의 원자적 비교·교체로 같은 refresh의 동시 갱신은 하나만 성공한다. 먼저 읽은 상태가 다른 요청에 의해 변경되면 409,
이미 교체된 refresh를 새로 제출하면 401이다. 실패 응답은 쿠키를 삭제하거나 덮어쓰지 않는다.
웹은 여러 탭까지 고려해 갱신을 직렬화하고, 갱신과 로그아웃도 동시에 보내지 않아야 한다.
409는 진행 중인 요청을 기다린 뒤 최신 쿠키로 제한적으로 재시도하고,
다른 요청의 갱신이 확인된 401도 최신 쿠키로 한 번만 재시도한다. 재시도에도 인증되지 않으면 재로그인한다.
응답 자체를 받지 못해 교체된 refresh 쿠키를 잃은 경우에는 재로그인이 필요하다.

비밀번호가 바뀌면 다음 재발급에서 기존 웹 세션을 거부한다. 비밀번호 변경과 동시에 모든 access가 폐기되는 정책은 아니다.
회원의 기기 전체 로그아웃 목록·관리 API는 이번 변경에 포함하지 않는다.

## SSR·웹뷰 연동 범위

전체 요청을 Next.js로 중계하는 BFF로 전환하지 않는다. SSR은 Next.js 서버에서 백엔드를 호출하고,
브라우저에서 발생하는 조회·생성·수정·삭제 요청은 기존처럼 백엔드를 직접 호출한다.
운영·stage의 SSR은 위 프로필별 공유 범위와 쿠키 이름을 사용한다. 프론트는 환경에 맞는 쿠키를 읽어야 한다.
공유 범위를 빈 값으로 덮어써 host-only를 사용하면 다른 호스트의 프론트 서버는 API 쿠키를 받을 수 없다.
SSR 서버는 받은 access 쿠키를 백엔드 `Cookie` 헤더로 전달하고 설정된 웹 `Origin`도 함께 전달한다.
웹 access 토큰을 기존 Bearer 경로로 변환하지 않는다. SSR 중 자동 재발급하는 새 흐름은 추가하지 않으며,
브라우저의 기존 재발급·재시도 처리를 `/v2/web/auth/refresh`와 CSRF 헤더에 연결한다.
SSR props, hydration, React Query 캐시 등 브라우저로 직렬화하는 데이터에 인증 토큰을 넣으면 안 된다.

앱 내부 웹뷰에서 토큰을 쿠키나 localStorage에 직접 주입하는 기존 연결은 별도 전환이 필요할 수 있다.
이 브랜치는 앱 웹뷰의 자동 로그인 교환이나 STOMP 쿠키 인증을 추가하지 않는다. 기존 STOMP 헤더 인증은 유지한다.
실제 브라우저·SSR·웹뷰·stage 도메인 통합 검증은 해당 클라이언트 전환 후 수행해야 한다.

## 확인 방법

Java 17과 Docker를 사용한다.

```sh
./gradlew build
```

`WebAuthApiTest`는 실제 테스트 DB·Redis를 연결한 MockMvc로 쿠키 속성, 앱 호환성,
권한 검사, 만료·재발급·로그아웃, CSRF·CORS, 토큰 전달 경로 분리를 검증한다.
`WebAuthSessionRedisRepositoryTest`는 실제 Redis에서 동시 갱신과 로그아웃 후 세션 복구 방지를 검증한다.
`WebAuthHttpTest`는 임의 포트의 실제 HTTP 서버에서 로그인·정보 조회·CSRF 헤더 누락 거부·CSRF 복구·재발급·로그아웃을 검증한다.
`webAuthHttpTest` 태스크로 별도 JVM에서 실행하며 `build`의 `check`에도 포함한다.
HTTP 클라이언트가 Cookie 헤더를 명시적으로 전달하는 테스트이며 실제 브라우저의 쿠키 저장/전송이나 프론트 SSR E2E는 아니다.
단위 테스트는 출처 검사, JWT 경로 분리, 세션 검증, 로컬 쿠키 설정과 로그 마스킹을 확인한다.
`WebAuthCompatibilityTest`는 기존 이메일 로그인, 관리자 접근 거부, 선택 인증의 개인화·익명 응답,
학생 쿠키 로그인과 학생 정보 조회를 확인한다.

Redis 장애는 서비스 mock 예외 주입과 컨트롤러의 500/409·Set-Cookie 미발급으로 검증한다.
실제 Redis 중단·복구나 네트워크 지연, 동시 HTTP 응답 순서, SQL 연결 풀 부하 시험은 수행하지 않았다.
탈퇴 계정 검증은 테스트 DB의 사용자 삭제·flush·clear 후 쿠키 접근과 재발급 거부를 확인한 것이며,
실제 탈퇴 API의 커밋·이벤트까지 포함한 E2E는 아니다.

### 로컬 검증 결과 (2026-09-10)

- 이슈: [KOIN_API_V2 #2424](https://github.com/BCSDLab/KOIN_API_V2/issues/2424)
- 로컬 브랜치: `feat/2424-web-httponly-auth`
- 기준 브랜치: `origin/develop`의 `3d14db39`
- Java 17, 실제 테스트 MySQL·Redis·MongoDB를 사용한 전체 `build` 성공
- 전체 1,243개 중 1,240개 통과, 기존 비활성 테스트 3개 건너뜀, 실패 0개
- 이번에 추가한 웹 인증 테스트 94개 모두 통과
- GET/HEAD 출처 검사와 잘못된 JSON 로그 마스킹 회귀 테스트 3개는 수정 전 실패, 수정 후 통과 확인
- 보안·세션·호환성 에이전트의 1차 검토를 반영하고 수정 후 재검토 완료. 추가 차단 결함 없음
- 로컬 `build/`에 중복된 `Test 2.class` 산출물이 발견되어, 검증에서는 임시 Gradle init script로
  buildDirectory만 `/tmp/koin-backend-2424-build`로 분리했다. 프로젝트의 Gradle 빌드 설정은 변경하지 않았다.
- 이번 작업에서는 프론트·앱 코드를 변경하지 않았고 실제 웹 화면·SSR·앱 웹뷰 연동과 운영/stage 검증은 수행하지 않았다.

쿠키 동작은 [MDN Set-Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Set-Cookie),
요청 위조 방어는 [OWASP CSRF 방어 지침](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)을 참고했다.
