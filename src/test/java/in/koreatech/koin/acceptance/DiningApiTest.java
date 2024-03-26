package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static io.restassured.RestAssured.given;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import io.restassured.http.ContentType;

class DiningApiTest extends AcceptanceTest {

    @Autowired
    private DiningRepository diningRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("특정 날짜의 모든 식단들을 조회한다.")
    void findDinings() {
        Dining request1 = Dining.builder()
            .id(1L)
            .date("2024-03-11")
            .type("LUNCH")
            .place("A코스")
            .priceCard(6000)
            .priceCash(6000)
            .kcal(881)
            .menu("""
                ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕"]""")
            .build();

        Dining request2 = Dining.builder()
            .id(2L)
            .date("2024-03-01")
            .type("LUNCH")
            .place("2캠퍼스")
            .priceCard(6000)
            .priceCash(6000)
            .kcal(881)
            .menu("""
                ["혼합잡곡밥", "가쓰오장국", "땡초부추전", "누룽지탕"]""")
            .build();

        Dining request3 = Dining.builder()
            .id(3L)
            .date("2024-03-11")
            .type("LUNCH")
            .place("능수관")
            .priceCard(6000)
            .priceCash(6000)
            .kcal(300)
            .menu("""
                ["참치김치볶음밥", "유부된장국", "땡초부추전", "누룽지탕"]""")
            .build();

        Dining dining1 = diningRepository.save(request1);
        Dining dining2 = diningRepository.save(request2);
        Dining dining3 = diningRepository.save(request3);

        var response = given()
            .when()
            .get("/dinings?date=240311")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        List<String> menus1 = List.of("병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕");
        List<String> menus2 = List.of("참치김치볶음밥", "유부된장국", "땡초부추전", "누룽지탕");

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getList(".").size()).isEqualTo(2);

                softly.assertThat(response.body().jsonPath().getLong("[0].id")).isEqualTo(dining1.getId());
                softly.assertThat(response.body().jsonPath().getString("[0].date")).isEqualTo(dining1.getDate());
                softly.assertThat(response.body().jsonPath().getString("[0].type")).isEqualTo(dining1.getType());
                softly.assertThat(response.body().jsonPath().getString("[0].place")).isEqualTo(dining1.getPlace());
                softly.assertThat(response.body().jsonPath().getInt("[0].price_card"))
                    .isEqualTo(dining1.getPriceCard());
                softly.assertThat(response.body().jsonPath().getInt("[0].price_cash"))
                    .isEqualTo(dining1.getPriceCash());
                softly.assertThat(response.body().jsonPath().getInt("[0].kcal")).isEqualTo(dining1.getKcal());
                softly.assertThat(response.body().jsonPath().getString("[0].created_at"))
                    .isEqualTo(dining1.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                softly.assertThat(response.body().jsonPath().getString("[0].updated_at"))
                    .isEqualTo(dining1.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                softly.assertThat(response.body().jsonPath().getList("[0].menu", String.class))
                    .containsExactlyInAnyOrderElementsOf(menus1);

                softly.assertThat(response.body().jsonPath().getLong("[1].id")).isEqualTo(dining3.getId());
                softly.assertThat(response.body().jsonPath().getString("[1].date")).isEqualTo(dining3.getDate());
                softly.assertThat(response.body().jsonPath().getString("[1].type")).isEqualTo(dining3.getType());
                softly.assertThat(response.body().jsonPath().getString("[1].place")).isEqualTo(dining3.getPlace());
                softly.assertThat(response.body().jsonPath().getInt("[1].price_card"))
                    .isEqualTo(dining3.getPriceCard());
                softly.assertThat(response.body().jsonPath().getInt("[1].price_cash"))
                    .isEqualTo(dining3.getPriceCash());
                softly.assertThat(response.body().jsonPath().getInt("[1].kcal")).isEqualTo(dining3.getKcal());
                softly.assertThat(response.body().jsonPath().getString("[1].created_at"))
                    .isEqualTo(dining3.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                softly.assertThat(response.body().jsonPath().getString("[1].updated_at"))
                    .isEqualTo(dining3.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                softly.assertThat(response.body().jsonPath().getList("[1].menu", String.class))
                    .containsExactlyInAnyOrderElementsOf(menus2);

            }
        );
    }

    @Test
    @DisplayName("잘못된 형식의 날짜로 조회한다. - 날짜의 형식이 잘못되었다면 400")
    void invalidFormatDate() {
        Dining request = Dining.builder()
            .id(1L)
            .date("2024-03-11")
            .type("LUNCH")
            .place("A코스")
            .priceCard(6000)
            .priceCash(6000)
            .kcal(881)
            .menu("""
                ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕"]""")
            .build();

        Dining dining = diningRepository.save(request);

        given()
            .when()
            .get("/dinings?date=20240311")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("날짜가 비어있다. - 오늘 날짜를 받아 조회한다.")
    void nullDate() {
        when(clock.instant()).thenReturn(
            ZonedDateTime.parse("2024-03-16 18:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z")).toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        Dining request1 = Dining.builder()
            .id(1L)
            .date("2024-03-16")
            .type("LUNCH")
            .place("A코스")
            .priceCard(6000)
            .priceCash(6000)
            .kcal(881)
            .menu("""
                ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "고구마순들깨볶음", "총각김치"]""")
            .build();

        Dining request2 = Dining.builder()
            .id(2L)
            .date("2024-03-13")
            .type("LUNCH")
            .place("2캠퍼스")
            .priceCard(6000)
            .priceCash(6000)
            .kcal(881)
            .menu("""
                ["혼합잡곡밥", "가쓰오장국", "땡초부추전", "누룽지탕"]""")
            .build();

        Dining dining1 = diningRepository.save(request1);
        Dining dining2 = diningRepository.save(request2);

        List<String> menus = List.of("병아리콩밥", "(탕)소고기육개장", "땡초부추전", "고구마순들깨볶음", "총각김치");

        var response = given()
            .when()
            .get("/dinings")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getList(".").size()).isEqualTo(1);

                softly.assertThat(response.body().jsonPath().getLong("[0].id")).isEqualTo(dining1.getId());
                softly.assertThat(response.body().jsonPath().getString("[0].date")).isEqualTo(dining1.getDate());
                softly.assertThat(response.body().jsonPath().getString("[0].type")).isEqualTo(dining1.getType());
                softly.assertThat(response.body().jsonPath().getString("[0].place")).isEqualTo(dining1.getPlace());
                softly.assertThat(response.body().jsonPath().getInt("[0].price_card"))
                    .isEqualTo(dining1.getPriceCard());
                softly.assertThat(response.body().jsonPath().getInt("[0].price_cash"))
                    .isEqualTo(dining1.getPriceCash());
                softly.assertThat(response.body().jsonPath().getInt("[0].kcal")).isEqualTo(dining1.getKcal());
                softly.assertThat(response.body().jsonPath().getString("[0].created_at"))
                    .isEqualTo(dining1.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                softly.assertThat(response.body().jsonPath().getString("[0].updated_at"))
                    .isEqualTo(dining1.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                softly.assertThat(response.body().jsonPath().getList("[0].menu", String.class))
                    .containsExactlyInAnyOrderElementsOf(menus);
            }
        );
    }

    @Test
    @DisplayName("영양사님 권한으로 식단 이미지를 업로드한다. - 이미지 URL이 DB에 저장된다.")
    void ImageUpload() {
        User coop = User.builder()
            .password("1234")
            .nickname("춘식")
            .name("황현식")
            .phoneNumber("010-1234-5678")
            .userType(COOP)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();
        userRepository.save(coop);

        String token = jwtProvider.createToken(coop);

        Dining request = Dining.builder()
            .id(1L)
            .date("2024-03-11")
            .type("LUNCH")
            .place("A코스")
            .priceCard(6000)
            .priceCash(6000)
            .kcal(881)
            .menu("""
                ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕"]""")
            .build();

        Dining dining = diningRepository.save(request);

        DiningImageRequest imageUrl = new DiningImageRequest(1L, "https://stage.koreatech.in/image.jpg");

        given()
            .body(imageUrl)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .patch("/coop/dining/image")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly ->
                softly.assertThat(diningRepository.getById(request.getId()).getImageUrl())
                    .isEqualTo(imageUrl.imageUrl())
        );
    }

    @Test
    @DisplayName("허용되지 않은 권한으로 식단 이미지를 업로드한다. - 권한 오류.")
    void ImageUploadWithNoAuth() {
        User user = User.builder()
            .password("1234")
            .nickname("춘식")
            .name("황현식")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);

        String token = jwtProvider.createToken(user);

        Dining request = Dining.builder()
            .id(1L)
            .date("2024-03-11")
            .type("LUNCH")
            .place("A코스")
            .priceCard(6000)
            .priceCash(6000)
            .kcal(881)
            .menu("""
                ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕"]""")
            .build();

        Dining dining = diningRepository.save(request);

        DiningImageRequest imageUrl = new DiningImageRequest(1L, "https://stage.koreatech.in/image.jpg");

        given()
            .body(imageUrl)
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .when()
            .patch("/coop/dining/image")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }
}
