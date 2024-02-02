package in.koreatech.koin.acceptance;

import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class AuthApiTest extends AcceptanceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTokenRepository tokenRepository;

    @Test
    @DisplayName("사용자가 로그인을 수행한다")
    void userLoginSuccess() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .body("""
                {
                  "email": "test@koreatech.ac.kr",
                  "password": "1234"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .log().all()
            .post("/user/login")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        User userResult = userRepository.findById(user.getId()).get();
        UserToken token = tokenRepository.findById(userResult.getId()).get();

        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getString("token")).isNotNull();
                softly.assertThat(response.jsonPath().getString("refresh_token")).isNotNull();
                softly.assertThat(response.jsonPath().getString("refresh_token"))
                    .isEqualTo(token.getRefreshToken());
                softly.assertThat(response.jsonPath().getString("user_type")).isEqualTo("STUDENT");
                softly.assertThat(userResult.getLastLoggedAt()).isNotNull();
            }
        );
    }

    @Test
    @DisplayName("사용자가 로그인 이후 로그아웃을 수행한다")
    void userLogoutSuccess() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .body("""
                {
                  "email": "test@koreatech.ac.kr",
                  "password": "1234"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .log().all()
            .post("/user/login")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        RestAssured
            .given()
            .log().all()
            .header("Authorization", "Bearer " + response.jsonPath().getString("token"))
            .when()
            .log().all()
            .post("/user/logout")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Optional<UserToken> token = tokenRepository.findById(user.getId());

        Assertions.assertThat(token).isEmpty();
    }

    @Test
    @DisplayName("사용자가 로그인 이후 refreshToken을 재발급한다")
    void userRefreshToken() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .body("""
                {
                  "email": "test@koreatech.ac.kr",
                  "password": "1234"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .log().all()
            .post("/user/login")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        RestAssured
            .given()
            .log().all()
            .body(
                Map.of("refresh_token", response.jsonPath().getString("refresh_token"))
            )
            .contentType(ContentType.JSON)
            .when()
            .log().all()
            .post("/user/refresh")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        UserToken token = tokenRepository.findById(user.getId()).get();

        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getString("token")).isNotNull();
                softly.assertThat(response.jsonPath().getString("refresh_token")).isNotNull();
                softly.assertThat(response.jsonPath().getString("refresh_token"))
                    .isEqualTo(token.getRefreshToken());
            }
        );
    }
}
