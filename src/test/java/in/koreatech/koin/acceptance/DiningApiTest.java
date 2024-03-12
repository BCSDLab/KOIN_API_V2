package in.koreatech.koin.acceptance;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class DiningApiTest extends AcceptanceTest {

    @Autowired
    private DiningRepository diningRepository;

    @Test
    @DisplayName("오늘 날짜의 모든 식단들을 조회한다.")
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
            ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕"]
            """)
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
            ["혼합잡곡밥", "가쓰오장국", "땡초부추전", "누룽지탕"]
            """)
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
            ["참치김치볶음밥", "유부된장국", "땡초부추전", "누룽지탕"]
            """)
            .build();

        Dining dining1 = diningRepository.save(request1);
        Dining dining2 = diningRepository.save(request2);
        Dining dining3 = diningRepository.save(request3);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/dinings?date=240311")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

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
                softly.assertThat(response.body().jsonPath().getString("[0].menu")).isEqualTo(dining1.getMenu());

                softly.assertThat(response.body().jsonPath().getLong("[1].id")).isEqualTo(dining3.getId());
                softly.assertThat(response.body().jsonPath().getString("[1].date")).isEqualTo(dining3.getDate());
                softly.assertThat(response.body().jsonPath().getString("[1].type")).isEqualTo(dining3.getType());
                softly.assertThat(response.body().jsonPath().getString("[1].place")).isEqualTo(dining3.getPlace());
                softly.assertThat(response.body().jsonPath().getInt("[1].price_card"))
                    .isEqualTo(dining3.getPriceCard());
                softly.assertThat(response.body().jsonPath().getInt("[1].price_cash"))
                    .isEqualTo(dining3.getPriceCash());
                softly.assertThat(response.body().jsonPath().getInt("[1].kcal")).isEqualTo(dining3.getKcal());
                softly.assertThat(response.body().jsonPath().getString("[1].menu")).isEqualTo(dining3.getMenu());
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
            ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕"]
            """)
            .build();

        Dining dining = diningRepository.save(request);

        RestAssured
            .given()
            .when()
            .get("/dinings?date=20240311")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("날짜가 비어있다. - 오늘 날짜를 받아 조회한다.")
    void nullDate() {
        Dining request1 = Dining.builder()
            .id(1L)
            .date("2024-03-12")
            .type("LUNCH")
            .place("A코스")
            .priceCard(6000)
            .priceCash(6000)
            .kcal(881)
            .menu("""
            ["병아리콩밥", "(탕)소고기육개장", "땡초부추전", "누룽지탕"]
            """)
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
            ["혼합잡곡밥", "가쓰오장국", "땡초부추전", "누룽지탕"]
            """)
            .build();

        Dining dining1 = diningRepository.save(request1);
        Dining dining2 = diningRepository.save(request2);

        ExtractableResponse<Response> response = RestAssured
            .given()
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
                softly.assertThat(response.body().jsonPath().getString("[0].menu")).isEqualTo(dining1.getMenu());
            }
        );
    }

}
