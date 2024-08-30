package in.koreatech.koin.acceptance;

import static in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType.LUNCH;
import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.SHOP_EVENT;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.Device;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.AccessHistoryRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.fixture.DeviceFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class NotificationApiTest extends AcceptanceTest {

    @Autowired
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Autowired
    private DeviceFixture deviceFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccessHistoryRepository accessHistoryRepository;

    User user;
    Device device;
    String userToken;
    String deviceToken;
    String ipAddress;
    String userAgent;

    @BeforeEach
    void setUp() {
        user = userFixture.준호_학생().getUser();
        userToken = userFixture.getToken(user);
        deviceToken = "testToken";
        ipAddress = userFixture.아이피();
        device = deviceFixture.갤럭시(user.getId(), ipAddress, deviceToken);
        userAgent = userFixture.맥북userAgent헤더();
    }

    @Test
    @DisplayName("알림 구독 내역을 조회한다.")
    void getNotificationSubscribe() {
        //given
        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .subscribeType(SHOP_EVENT)
            .device(device)
            .build();

        notificationSubscribeRepository.save(notificationSubscribe);

        //when then
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "is_permit": false,
                    "subscribes": [
                        {
                            "type": "SHOP_EVENT",
                            "is_permit": true,
                            "detail_subscribes": [
                               \s
                            ]
                        },
                        {
                            "type": "DINING_SOLD_OUT",
                            "is_permit": false,
                            "detail_subscribes": [
                                {
                                    "detail_type": "BREAKFAST",
                                    "is_permit": false
                                },
                                {
                                    "detail_type": "LUNCH",
                                    "is_permit": false
                                },
                                {
                                    "detail_type": "DINNER",
                                    "is_permit": false
                                }
                            ]
                        },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                    ]
                }
                """);
    }

    @Test
    @DisplayName("전체 알림을 구독한다. - 디바이스 토큰을 추가한다.")
    void createDivceToken() {
        //when then
        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
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

        Device result = accessHistoryRepository.getByPublicIp(ipAddress).getDevice();
        assertThat(result.getFcmToken()).isEqualTo(deviceToken);
    }

    @Test
    @DisplayName("특정 알림을 구독한다.")
    void subscribeNotificationType() {
        String notificationType = SHOP_EVENT.name();

        RestAssured.given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
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
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
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
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                     "is_permit": true,
                     "subscribes": [
                         {
                             "type": "SHOP_EVENT",
                             "is_permit": true,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "DINING_SOLD_OUT",
                             "is_permit": false,
                             "detail_subscribes": [
                                 {
                                     "detail_type": "BREAKFAST",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "LUNCH",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "DINNER",
                                     "is_permit": false
                                 }
                             ]
                         },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                     ]
                 }
                """);
    }

    @Test
    @DisplayName("특정 세부알림을 구독한다.")
    void subscribeNotificationDetailType() {
        String notificationType = DINING_SOLD_OUT.name();
        String notificationDetailType = LUNCH.name();

        RestAssured.given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
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
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .contentType(ContentType.JSON)
            .queryParam("type", notificationType)
            .when()
            .post("/notification/subscribe")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .contentType(ContentType.JSON)
            .queryParam("detail_type", notificationDetailType)
            .when()
            .post("/notification/subscribe/detail")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                     "is_permit": true,
                     "subscribes": [
                         {
                             "type": "SHOP_EVENT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "DINING_SOLD_OUT",
                             "is_permit": true,
                             "detail_subscribes": [
                                 {
                                     "detail_type": "BREAKFAST",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "LUNCH",
                                     "is_permit": true
                                 },
                                 {
                                     "detail_type": "DINNER",
                                     "is_permit": false
                                 }
                             ]
                         },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                     ]
                 }
                """);
    }

    @Test
    @DisplayName("전체 알림 구독을 취소한다. - 디바이스 토큰을 삭제한다.")
    void deleteDeviceToken() {
        device.permitNotification(deviceToken);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .when()
            .delete("/notification")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        Device result = accessHistoryRepository.getByPublicIp(ipAddress).getDevice();
        assertThat(result.getFcmToken()).isNull();
    }

    @Test
    @DisplayName("특정 알림 구독을 취소한다.")
    void unsubscribeNotificationType() {
        var SubscribeShopEvent = NotificationSubscribe.builder()
            .subscribeType(SHOP_EVENT)
            .device(device)
            .build();

        var SubscribeDiningSoldOut = NotificationSubscribe.builder()
            .subscribeType(NotificationSubscribeType.DINING_SOLD_OUT)
            .device(device)
            .build();

        notificationSubscribeRepository.save(SubscribeShopEvent);
        notificationSubscribeRepository.save(SubscribeDiningSoldOut);

        String notificationType = SHOP_EVENT.name();

        RestAssured.given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
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
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .queryParam("type", notificationType)
            .when()
            .delete("/notification/subscribe")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                     "is_permit": true,
                     "subscribes": [
                         {
                             "type": "SHOP_EVENT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "DINING_SOLD_OUT",
                             "is_permit": true,
                             "detail_subscribes": [
                                 {
                                     "detail_type": "BREAKFAST",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "LUNCH",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "DINNER",
                                     "is_permit": false
                                 }
                             ]
                         },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                     ]
                 }
                """);
    }

    @Test
    @DisplayName("특정 세부 알림 구독을 취소한다.")
    void unsubscribeNotificationDetailType() {
        var SubscribeDiningSoldOut = NotificationSubscribe.builder()
            .subscribeType(NotificationSubscribeType.DINING_SOLD_OUT)
            .device(device)
            .build();

        var SubscribeBreakfast = NotificationSubscribe.builder()
            .detailType(NotificationDetailSubscribeType.BREAKFAST)
            .subscribeType(NotificationSubscribeType.DINING_SOLD_OUT)
            .device(device)
            .build();

        var SubscribeLunch = NotificationSubscribe.builder()
            .detailType(NotificationDetailSubscribeType.LUNCH)
            .subscribeType(NotificationSubscribeType.DINING_SOLD_OUT)
            .device(device)
            .build();

        notificationSubscribeRepository.save(SubscribeDiningSoldOut);
        notificationSubscribeRepository.save(SubscribeBreakfast);
        notificationSubscribeRepository.save(SubscribeLunch);

        String notificationType = DINING_SOLD_OUT.name();
        String notificationDetailType = LUNCH.name();

        RestAssured.given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
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
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .contentType(ContentType.JSON)
            .queryParam("type", notificationType)
            .when()
            .post("/notification/subscribe")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .queryParam("detail_type", notificationDetailType)
            .when()
            .delete("/notification/subscribe/detail")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + userToken)
            .header("User-Agent", userAgent)
            .header("X-Forwarded-For", ipAddress)
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                     "is_permit": true,
                     "subscribes": [
                         {
                             "type": "SHOP_EVENT",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         },
                         {
                             "type": "DINING_SOLD_OUT",
                             "is_permit": true,
                             "detail_subscribes": [
                                 {
                                     "detail_type": "BREAKFAST",
                                     "is_permit": true
                                 },
                                 {
                                     "detail_type": "LUNCH",
                                     "is_permit": false
                                 },
                                 {
                                     "detail_type": "DINNER",
                                     "is_permit": false
                                 }
                             ]
                         },
                         {
                             "type": "DINING_IMAGE_UPLOAD",
                             "is_permit": false,
                             "detail_subscribes": [
                                \s
                             ]
                         }
                     ]
                 }
                """);
    }
}
