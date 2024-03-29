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
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class NotificationApiTest extends AcceptanceTest {

    @Autowired
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private static User user;

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

    }

    @Test
    @DisplayName("알림 구독 내역 조회한다.")
    void getNotifictionSubscribe() {

        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .subscribeType(NotificationSubscribeType.SHOP_EVENT)
            .user(user)
            .build();

        notificationSubscribeRepository.save(notificationSubscribe);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("lands[1].charter_fee"))
                    .isEqualTo(land2.getCharterFee());
            }
        );
    }
}
