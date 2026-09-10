# 웹 HttpOnly 쿠키 인증

기존 `/v2/users/login`, `/user/login`, `/user/refresh`, `/user/logout`의 앱용 계약은 유지한다.
웹(PC·모바일 브라우저)은 아래 API로 전환한다. User-Agent의 PC/Mobile 분류로 인증 방식을 선택하지 않는다.
이번 변경은 일반·학생·총학생회 사용자의 웹 인증을 대상으로 하며 사장님·영양사·관리자 로그인은 기존 API를 사용한다.

## API 계약

| 요청 | 입력 | 성공 응답 |
| --- | --- | --- |
| `POST /v2/web/auth/login` | JSON `login_id`, `login_pw`, `auto_login` | 201, 인증 쿠키 2개, `user_type`·`csrf_token` |
| `GET /v2/web/auth/csrf` | refresh 쿠키 | 200, `csrf_token` |
| `POST /v2/web/auth/refresh` | refresh 쿠키, `X-CSRF-Token` | 201, 교체된 인증 쿠키 2개, `user_type`·`csrf_token` |
| `POST /v2/web/auth/logout` | refresh 쿠키, `X-CSRF-Token` | 204, 현재 웹 세션 폐기 및 쿠키 삭제 |

웹 인증 API는 허용된 `Origin` 또는 `Referer`가 필요하다. 로그인 본문은 `application/json`만 받는다.
`login_pw`는 기존 로그인과 동일하게 SHA-256 처리한 비밀번호를 전달한다.
`auto_login`은 생략하면 false이며, false일 때는 두 쿠키 모두 브라우저 세션 쿠키다.
브라우저의 세션 복원 기능 때문에 브라우저 종료가 서버 세션의 즉시 폐기를 의미하지는 않는다.

access·refresh 값은 JSON 응답에 포함하지 않는다. `csrf_token`은 인증 토큰과 별개의 난수로,
웹 코드가 메모리에 보관하고 쿠키로 인증하는 POST/PUT/PATCH/DELETE 요청의 `X-CSRF-Token` 헤더에 넣는다.
새로고침으로 메모리의 CSRF 토큰을 잃으면 `/csrf`에서 다시 조회한다. 로그인 시에는 기존 세션이 없으므로
허용 출처 검사와 JSON Content-Type 제한으로 로그인 CSRF를 방어한다.

## 웹 연동 순서

1. 모든 웹 API 요청에 `credentials: 'include'` 또는 Axios의 `withCredentials: true`를 설정한다.
2. 로그인 성공 시 `user_type`과 `csrf_token`만 메모리에 보관한다.
3. 기존 `document.cookie`, localStorage, Zustand의 access·refresh 저장 및 Bearer 헤더 주입을 제거한다.
4. 쿠키 인증의 상태 변경 요청에는 CSRF 헤더를 추가한다. 조회 요청은 쿠키만으로 인증한다.
5. access 만료 시 `/refresh`를 한 번 호출한 뒤 원래 요청을 재시도한다.
6. 로그아웃은 서버 `/logout`의 성공을 확인한 뒤 화면의 로그인 상태를 비운다.

로그인·재발급·로그아웃·CSRF 조회는 만료된 access 쿠키의 영향을 받지 않는다.
일반 기능 API에서는 명시적인 `Authorization` 헤더가 있으면 기존 헤더 인증만 사용한다.
잘못된 헤더를 쿠키 인증으로 대체하지 않으므로, 웹 전환 시 예전 헤더 주입 코드도 제거해야 한다.
기존 앱 토큰을 웹 쿠키로 넣거나 웹 토큰을 기존 앱 인증 경로로 전달하는 것은 허용하지 않는다.
일반 API의 쿠키 인증은 기존 `@Auth`·`@UserId` 파라미터가 있는 메서드에 적용한다.
회원가입·비밀번호 재설정 등 비인증 API는 남아 있는 쿠키 때문에 인증이나 CSRF 헤더를 요구하지 않는다.

## 쿠키와 만료 설정

| 항목 | 운영 기본값 |
| --- | --- |
| access 쿠키 | `__Host-koin-web-access`, Path `/`, 기본 15분 |
| refresh 쿠키 | `__Secure-koin-web-refresh`, Path `/v2/web/auth`, 기본 90일 |
| 공통 속성 | HttpOnly, Secure, SameSite=Lax, Domain 미지정(host-only) |
| 자동 로그인 false | Max-Age 없는 세션 쿠키. 서버의 만료 시간은 그대로 적용 |

`WEB_AUTH_ACCESS_TOKEN_TTL`, `WEB_AUTH_REFRESH_TOKEN_TTL`, `WEB_AUTH_COOKIE_SECURE`,
`WEB_AUTH_COOKIE_SAME_SITE` 환경변수로 설정한다. 기존 앱의 JWT 만료 설정은 바꾸지 않는다.
`local` 프로필에서는 HTTP 개발을 위해 Secure=false와 `koin-web-access`·`koin-web-refresh` 이름을 사용한다.
운영에서는 Secure를 유지해야 하며 SameSite=None은 Secure 없이 설정할 수 없다.
`CORS_ALLOWED_ORIGINS`에는 실제 웹의 정확한 origin을 넣는다. `*`나 도메인 접미사 비교로 웹 요청을 허용하지 않는다.

## 세션과 동시 요청

웹은 로그인마다 `webAuthSession:<랜덤 세션 ID>`를 생성한다. 앱의 기존 Redis 키와 독립적이며 DB 마이그레이션은 없다.
refresh 원문은 저장하지 않고 SHA-256 해시를 저장한다. 재발급 시 refresh를 교체하되 로그인 시 정한 절대 만료 시간은 연장하지 않는다.
웹 access JWT에도 세션 ID를 넣고 요청마다 Redis 세션을 확인하므로 웹 로그아웃 후 남은 access도 사용할 수 없다.
이 때문에 쿠키 인증은 Redis 가용성에 의존하며, Redis 조회 실패 시 인증을 허용하지 않는다.

Redis의 원자적 비교·교체로 같은 refresh의 동시 갱신은 하나만 성공한다. 먼저 읽은 상태가 다른 요청에 의해 변경되면 409,
이미 교체된 refresh를 새로 제출하면 401이다. 실패 응답은 쿠키를 삭제하거나 덮어쓰지 않는다.
웹은 여러 탭까지 고려해 갱신을 직렬화하고, 갱신과 로그아웃도 동시에 보내지 않아야 한다.
409는 진행 중인 요청을 기다린 뒤 최신 쿠키로 제한적으로 재시도하고,
다른 요청의 갱신이 확인된 401도 최신 쿠키로 한 번만 재시도한다. 재시도에도 인증되지 않으면 재로그인한다.
응답 자체를 받지 못해 교체된 refresh 쿠키를 잃은 경우에는 재로그인이 필요하다.

비밀번호가 바뀌면 다음 재발급에서 기존 웹 세션을 거부한다. 비밀번호 변경과 동시에 모든 access가 폐기되는 정책은 아니다.
회원의 기기 전체 로그아웃 목록·관리 API는 이번 변경에 포함하지 않는다.

## SSR·웹뷰 연동 범위

쿠키는 API 호스트에만 저장한다. 프런트 서버가 다른 호스트에 있다면 프런트 SSR 요청에서 이 쿠키를 바로 받을 수 없다.
현재 웹의 SSR 인증은 배포 도메인 구성에 맞춰 별도 연동해야 하며, 프런트와 같은 origin의 프록시/BFF를 사용하는 방안 등을 검토한다.
프록시는 인증 경로와 Cookie/Set-Cookie를 올바르게 전달하고, 서버 요청에는 설정된 웹 origin을 전달해야 한다.
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
단위 테스트는 출처 검사, JWT 경로 분리, 세션 검증, 로컬 쿠키 설정과 로그 마스킹을 확인한다.

### 로컬 검증 결과 (2026-09-10)

- 기준 브랜치: `origin/develop`의 `3d14db39`
- Java 17, 실제 테스트 MySQL·Redis·MongoDB를 사용한 전체 `build` 성공
- 전체 1,216개 중 1,213개 통과, 기존 비활성 테스트 3개 건너뜀, 실패 0개
- 이번에 추가한 인증 테스트 67개 모두 통과
- 로컬 `build/`에 중복된 `Test 2.class` 산출물이 발견되어, 검증에서는 임시 Gradle init script로
  buildDirectory만 `/tmp/koin-web-auth-build-20260910`으로 분리했다. 프로젝트의 Gradle 빌드 설정은 변경하지 않았다.
- 실제 웹 화면·SSR·앱 웹뷰 연동과 운영/stage 검증은 수행하지 않았다.

쿠키 동작은 [MDN Set-Cookie](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Set-Cookie),
요청 위조 방어는 [OWASP CSRF 방어 지침](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)을 참고했다.
