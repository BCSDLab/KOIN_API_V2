package in.koreatech.koin.acceptance;


import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

import in.koreatech.koin.domain.user.User;
import in.koreatech.koin.domain.user.UserType;
import in.koreatech.koin.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class UserApiTest {

    @LocalServerPort
    int port;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("사용자가 로그인을 수행한다")
    void userLogin() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(UserType.STUDENT)
            .email("test@example.com")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .body("""
                {
                  "email": "test@example.com",
                  "password": "1234"
                }
                """)
            .when()
            .log().all()
            .post("/user/login")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getString("token")).isNotNull();
                softly.assertThat(response.jsonPath().getString("refresh_token")).isNotNull();
                softly.assertThat(response.jsonPath().getString("user_type")).isEqualTo("STUDENT");
            }
        );
    }
}
