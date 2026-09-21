package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.transaction.TestTransaction;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.config.WebAuthProperties;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("web-auth-http")
class WebAuthHttpTest extends AcceptanceTest {

    private static final String AUTH_PATH = "/v2/web/auth";

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebAuthProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void 실제_HTTP에서_쿠키_발급_조회_재발급_로그아웃을_처리한다() throws Exception {
        User user = userRepository.save(User.builder()
            .loginId("web-http-test").loginPw(passwordEncoder.encode("1234"))
            .name("웹HTTP테스트").nickname("웹HTTP인증").phoneNumber("01055558888")
            .email("web-http-auth@koreatech.ac.kr").userType(UserType.GENERAL)
            .isAuthed(true).isDeleted(false).build());
        // 실제 HTTP 요청은 다른 트랜잭션에서 실행되므로 테스트 계정만 먼저 커밋한다.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        try {
            HttpResponse<String> login = send(request(AUTH_PATH + "/login")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                    {"login_id":"web-http-test","login_pw":"1234","auto_login":true}
                    """)));
            assertThat(login.statusCode()).isEqualTo(201);
            assertThat(login.headers().allValues("Set-Cookie")).hasSize(3);
            String access = cookie(login, properties.accessCookieName());
            String refresh = cookie(login, properties.refreshCookieName());
            String csrf = objectMapper.readTree(login.body()).path("csrf_token").asText();
            assertThat(cookie(login, properties.csrfCookieName())).isEqualTo(properties.csrfCookieName() + "=" + csrf);

            HttpResponse<String> me = send(request("/v2/users/me").header("Cookie", access).GET());
            assertThat(me.statusCode()).isEqualTo(200);
            assertThat(objectMapper.readTree(me.body()).path("login_id").asText()).isEqualTo("web-http-test");

            HttpResponse<String> missingCsrf = send(request(AUTH_PATH + "/refresh")
                .header("Cookie", refresh + "; " + cookie(login, properties.csrfCookieName()))
                .POST(HttpRequest.BodyPublishers.noBody()));
            assertThat(missingCsrf.statusCode()).isEqualTo(403);
            assertThat(missingCsrf.headers().allValues("Set-Cookie")).isEmpty();

            HttpResponse<String> restored = send(request(AUTH_PATH + "/csrf").header("Cookie", refresh).GET());
            assertThat(restored.statusCode()).isEqualTo(200);
            assertThat(restored.headers().allValues("Set-Cookie")).hasSize(1);

            HttpResponse<String> renewed = send(request(AUTH_PATH + "/refresh")
                .header("Cookie", refresh).header("X-CSRF-Token", csrf).POST(HttpRequest.BodyPublishers.noBody()));
            assertThat(renewed.statusCode()).isEqualTo(201);
            String nextRefresh = cookie(renewed, properties.refreshCookieName());
            assertThat(nextRefresh).isNotEqualTo(refresh);
            assertThat(cookie(renewed, properties.csrfCookieName())).isEqualTo(cookie(login, properties.csrfCookieName()));

            HttpResponse<String> logout = send(request(AUTH_PATH + "/logout")
                .header("Cookie", nextRefresh).header("X-CSRF-Token", csrf).POST(HttpRequest.BodyPublishers.noBody()));
            assertThat(logout.statusCode()).isEqualTo(204);
            assertThat(logout.headers().allValues("Set-Cookie")).hasSize(3)
                .allSatisfy(value -> assertThat(value).contains("Max-Age=0"));
            assertThat(send(request("/user/auth").header("Cookie", access).GET()).statusCode()).isEqualTo(401);
        } finally {
            userRepository.delete(user);
        }
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .timeout(Duration.ofSeconds(10)).header("Origin", "http://localhost:3000");
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String cookie(HttpResponse<String> response, String name) {
        return response.headers().allValues("Set-Cookie").stream()
            .filter(value -> value.startsWith(name + "=")).map(value -> value.split(";", 2)[0])
            .findFirst().orElseThrow();
    }
}
