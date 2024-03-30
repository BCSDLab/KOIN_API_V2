package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.SHOP_EVENT;
import static org.assertj.core.api.Assertions.assertThat;

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
            .subscribeType(SHOP_EVENT)
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
    @DisplayName("전체 알림을 구독한다. - 디바이스 토큰을 추가한다.")
    void createDivceToken() {
        //given
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
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        User changedDeviceTokenUser = userRepository.getById(user.getId());
        assertThat(changedDeviceTokenUser.getDeviceToken()).isEqualTo(deviceToken);
    }

    @Test
    @DisplayName("특정 알림을 구독한다.")
    void subscribeNotificationType() {

        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .body("""
                {
                "type": "SHOP_EVENT"
                }
                """)
            .when()
            .post("/notification/subscribe")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

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
    @DisplayName("전체 알림 구독을 취소한다. - 디바이스 토큰을 삭제한다.")
    void deleteDeviceToken() {

        String deviceToken = "testToken";

        user.permitNotification(deviceToken);
        userRepository.save(user);
        User changedDeviceTokenUser = userRepository.getById(user.getId());

        assertThat(changedDeviceTokenUser.getDeviceToken()).isEqualTo(deviceToken);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .when()
            .delete("/notification")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        User noneDeviceTokenUser = userRepository.getById(user.getId());
        assertThat(noneDeviceTokenUser.getDeviceToken()).isNull();
    }

    @Test
    @DisplayName("특정 알림 구독을 취소한다.")
    void unsubscribeNotificationType() {

        var SubscribeShopEvent = NotificationSubscribe.builder()
            .subscribeType(SHOP_EVENT)
            .user(user)
            .build();

        var SubscribeDiningSoldOut = NotificationSubscribe.builder()
            .subscribeType(NotificationSubscribeType.DINING_SOLD_OUT)
            .user(user)
            .build();

        notificationSubscribeRepository.save(SubscribeShopEvent);
        notificationSubscribeRepository.save(SubscribeDiningSoldOut);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .param("type", SHOP_EVENT)
            .when()
            .delete("/notification/subscribe")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

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
                softly.assertThat(response.body().jsonPath().getBoolean("subscribes[0].is_permit")).isFalse();
                softly.assertThat(response.body().jsonPath().getString("subscribes[1].type")).isEqualTo(
                    "DINING_SOLD_OUT");
                softly.assertThat(response.body().jsonPath().getBoolean("subscribes[1].is_permit")).isTrue();
            }
        );
    }
}
