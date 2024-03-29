package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class NotificationApiTest extends AcceptanceTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private static User user;

    private static String userToken;

    @BeforeEach
    void setUp() {
        User newUser = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();
        user = userRepository.save(newUser);
        userToken = jwtProvider.createToken(user);
    }

    @Test
    @DisplayName("알림 구독 내역 조회한다.")
    void getNotificationSubscribe() {
        //given
        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .subscribeType(NotificationSubscribeType.SHOP_EVENT)
            .user(user)
            .build();

        notificationSubscribeRepository.save(notificationSubscribe);
        //when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getBoolean("is_permit")).isFalse();
                softly.assertThat(response.body().jsonPath().getList("subscribes").size()).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getString("subscribes[0].type"))
                    .isEqualTo("SHOP_EVENT");
                softly.assertThat(response.body().jsonPath().getBoolean("subscribes[0].is_permit")).isTrue();
                softly.assertThat(response.body().jsonPath().getString("subscribes[1].type")).isEqualTo(
                    "DINING_SOLD_OUT");
                softly.assertThat(response.body().jsonPath().getBoolean("subscribes[1].is_permit")).isFalse();
            }
        );
    }

    @Test
    @DisplayName("디바이스토큰을 추가한다.")
    void createDivceToken() {
        //given

        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .subscribeType(NotificationSubscribeType.SHOP_EVENT)
            .user(user)
            .build();

        String deviceToken = "testToken";

        //when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .body(String.format("""
                    {
                      "device_token": "%s"
                    }
                """, deviceToken))

            .contentType(ContentType.JSON)
            .when()
            .post("/notification")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        User changedDeviceTokenUser = userRepository.getById(user.getId());

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(changedDeviceTokenUser.getDeviceToken()).isEqualTo(deviceToken);
            }
        );
    }
}
