package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

class AuthApiTest extends AcceptanceTest {

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTokenRepository tokenRepository;

    @Test
    @DisplayName("사용자가 로그인을 수행한다")
    void userLoginSuccess() {
        User user = userFixture.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        var response = RestAssured
            .given()
            .body("""
                {
                  "email": "test@koreatech.ac.kr",
                  "password": "1234"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/user/login")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        User userResult = userRepository.findById(user.getId()).get();
        UserToken token = tokenRepository.findById(userResult.getId()).get();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                    {
                        "token": "%s",
                        "refresh_token": "%s",
                        "user_type": "%s"
                    }
                    """,
                response.jsonPath().getString("token"),
                token.getRefreshToken(),
                user.getUserType().name()
            ));
    }

    @Test
    @DisplayName("사용자가 로그인 이후 로그아웃을 수행한다")
    void userLogoutSuccessg() {
        User user = userFixture.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        var response = RestAssured
            .given()
            .body("""
                {
                  "email": "test@koreatech.ac.kr",
                  "password": "1234"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/user/login")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        RestAssured
            .given()
            .header("Authorization", "Bearer " + response.jsonPath().getString("token"))
            .when()
            .post("/user/logout")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Assertions.assertThat(tokenRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("사용자가 로그인 이후 refreshToken을 재발급한다")
    void userRefreshToken() {
        User user = userFixture.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        var loginResponse = RestAssured
            .given()
            .body("""
                {
                  "email": "test@koreatech.ac.kr",
                  "password": "1234"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/user/login")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        var response = RestAssured
            .given()
            .body(String.format("""
                    {
                      "refresh_token": "%s"
                    }
                    """,
                loginResponse.jsonPath().getString("refresh_token"))
            )
            .contentType(ContentType.JSON)
            .when()
            .post("/user/refresh")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        UserToken token = tokenRepository.findById(user.getId()).get();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                    {
                        "token": "%s",
                        "refresh_token": "%s"
                    }
                    """,
                response.jsonPath().getString("token"),
                token.getRefreshToken()
            ));
    }
}
