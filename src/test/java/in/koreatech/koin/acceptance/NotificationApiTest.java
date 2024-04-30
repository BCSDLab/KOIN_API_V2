package in.koreatech.koin.acceptance;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.SHOP_EVENT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class NotificationApiTest extends AcceptanceTest {

    @Autowired
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private UserRepository userRepository;

    User user;
    String userToken;

    @BeforeEach
    void setUp() {
        user = userFixture.준호_학생().getUser();
        userToken = userFixture.getToken(user);
    }

    @Test
    @DisplayName("알림 구독 내역을 조회한다.")
    void getNotificationSubscribe() {
        //given
        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .subscribeType(SHOP_EVENT)
            .user(user)
            .build();

        notificationSubscribeRepository.save(notificationSubscribe);

        //when then
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "is_permit": false,
                    "subscribes": [
                        {
                            "type": "SHOP_EVENT",
                            "is_permit": true
                        },
                        {
                            "type": "DINING_SOLD_OUT",
                            "is_permit": false
                        }
                    ]
                }
                """);
    }

    @Test
    @DisplayName("전체 알림을 구독한다. - 디바이스 토큰을 추가한다.")
    void createDivceToken() {
        //given
        String deviceToken = "testToken";

        //when then
        RestAssured
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

        User result = userRepository.getById(user.getId());
        assertThat(result.getDeviceToken()).isEqualTo(deviceToken);
    }

    @Test
    @DisplayName("특정 알림을 구독한다.")
    void subscribeNotificationType() {
        String deviceToken = "testToken";
        String notificationType = SHOP_EVENT.name();

        RestAssured.given()
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

        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .contentType(ContentType.JSON)
            .queryParam("type", notificationType)
            .when()
            .post("/notification/subscribe")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "is_permit": true,
                    "subscribes": [
                        {
                            "type": "SHOP_EVENT",
                            "is_permit": true
                        },
                        {
                            "type": "DINING_SOLD_OUT",
                            "is_permit": false
                        }
                    ]
                }
                """);
    }

    @Test
    @DisplayName("전체 알림 구독을 취소한다. - 디바이스 토큰을 삭제한다.")
    void deleteDeviceToken() {
        String deviceToken = "testToken";
        user.permitNotification(deviceToken);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .when()
            .delete("/notification")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        User result = userRepository.getById(user.getId());
        assertThat(result.getDeviceToken()).isNull();
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

        String notificationType = SHOP_EVENT.name();

        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .queryParam("type", notificationType)
            .when()
            .delete("/notification/subscribe")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "is_permit": false,
                    "subscribes": [
                        {
                            "type": "SHOP_EVENT",
                            "is_permit": false
                        },
                        {
                            "type": "DINING_SOLD_OUT",
                            "is_permit": true
                        }
                    ]
                }
                """);
    }
}
