package in.koreatech.koin.acceptance;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.coop.model.DiningSoldOutCache;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.DiningFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class DiningApiTest extends AcceptanceTest {

    @Autowired
    private DiningRepository diningRepository;

    @Autowired
    private DiningSoldOutCacheRepository diningSoldOutCacheRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private DiningFixture diningFixture;

    private Dining A코너_점심;
    private User coop_준기;
    private String token_준기;
    private User owner_현수;
    private String token_현수;

    @BeforeEach
    void setUp() {
        coop_준기 = userFixture.준기_영양사().getUser();
        token_준기 = userFixture.getToken(coop_준기);
        owner_현수 = userFixture.현수_사장님().getUser();
        token_현수 = userFixture.getToken(owner_현수);
        A코너_점심 = diningFixture.A코스_점심(LocalDate.parse("2024-01-15"));
    }

    @Test
    @DisplayName("특정 날짜의 모든 식단들을 조회한다.")
    void findDinings() {
        var response = given()
            .when()
            .get("/dinings?date=240115")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "id": 1,
                        "date": "2024-01-15",
                        "type": "LUNCH",
                        "place": "A코스",
                        "price_card": 6000,
                        "price_cash": 6000,
                        "kcal": 881,
                        "menu": [
                            "병아리콩밥",
                            "(탕)소고기육개장",
                            "땡초부추전",
                            "누룽지탕"
                        ],
                        "image_url": null,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00",
                        "soldout_at": null,
                        "changed_at": null
                    }
                ]
                """);
    }

    @Test
    @DisplayName("잘못된 형식의 날짜로 조회한다. - 날짜의 형식이 잘못되었다면 400")
    void invalidFormatDate() {
        given()
            .when()
            .get("/dinings?date=20240115")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("날짜가 비어있다. - 오늘 날짜를 받아 조회한다.")
    void nullDate() {
        var response = given()
            .when()
            .get("/dinings")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "id": 1,
                        "date": "2024-01-15",
                        "type": "LUNCH",
                        "place": "A코스",
                        "price_card": 6000,
                        "price_cash": 6000,
                        "kcal": 881,
                        "menu": [
                            "병아리콩밥",
                            "(탕)소고기육개장",
                            "땡초부추전",
                            "누룽지탕"
                        ],
                        "image_url": null,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00",
                        "soldout_at": null,
                        "changed_at": null
                    }
                ]
                """);
    }

    @Test
    @DisplayName("영양사 권한으로 품절 요청을 보낸다.")
    void requestSoldOut() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준기)
            .body(String.format("""
                {
                    "menu_id": "%s",
                    "sold_out": %s
                }
                """, A코너_점심.getId(), true)
            )
            .when()
            .patch("/coop/dining/soldout")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();
    }

    @Test
    @DisplayName("권한이 없는 사용자가 품절 요청을 보낸다")
    void requestSoldOutNoAuth() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_현수)
            .body(String.format("""
                {
                    "menu_id": "%s",
                    "sold_out": %s
                }
                """, A코너_점심.getId(), true))
            .when()
            .patch("/coop/dining/soldout")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("영양사님 권한으로 식단 이미지를 업로드한다. - 이미지 URL이 DB에 저장된다.")
    void ImageUpload() {
        String imageUrl = "https://stage.koreatech.in/image.jpg";

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준기)
            .body(String.format("""
                {
                    "menu_id": "%s",
                    "image_url": "%s"
                }
                """, A코너_점심.getId(), imageUrl)
            )
            .when()
            .patch("/coop/dining/image")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Dining result = diningRepository.getById(A코너_점심.getId());
        assertThat(result.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("허용되지 않은 권한으로 식단 이미지를 업로드한다. - 권한 오류.")
    void ImageUploadWithNoAuth() {
        String imageUrl = "https://stage.koreatech.in/image.jpg";

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_현수)
            .body(String.format("""
                {
                    "menu_id": "%s",
                    "image_url": "%s"
                }
                """, A코너_점심.getId(), imageUrl)
            )
            .when()
            .patch("/coop/dining/image")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("해당 식사시간에 품절 요청을 한다. - 품절 알림이 발송된다.")
    void checkSoldOutNotification() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준기)
            .body(String.format("""
                {
                    "menu_id": "%s",
                    "sold_out": %s
                }
                """, A코너_점심.getId(), true))
            .when()
            .patch("/coop/dining/soldout")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        verify(coopEventListener).onDiningSoldOutRequest(any());
    }

    @Test
    @DisplayName("해당 식사시간 외에 품절 요청을 한다. - 품절 알림이 발송되지 않는다.")
    void checkSoldOutNotificationAfterHours() {
        Dining A코너_저녁 = diningFixture.A코스_저녁(LocalDate.parse("2024-01-15"));
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준기)
            .body(String.format("""
                {
                    "menu_id": "%s",
                    "sold_out": %s
                }
                """, A코너_저녁.getId(), true))
            .when()
            .patch("/coop/dining/soldout")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        verify(coopEventListener, never()).onDiningSoldOutRequest(any());
    }

    @Test
    @DisplayName("동일한 식단 코너의 두 번째 품절 요청은 알림이 가지 않는다.")
    void checkSoldOutNotificationResend() {
        diningSoldOutCacheRepository.save(DiningSoldOutCache.from(A코너_점심.getPlace()));

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준기)
            .body(String.format("""
                {
                    "menu_id": "%s",
                    "sold_out": %s
                }
                """, A코너_점심.getId(), true))
            .when()
            .patch("/coop/dining/soldout")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        verify(coopEventListener, never()).onDiningSoldOutRequest(any());
    }
}
