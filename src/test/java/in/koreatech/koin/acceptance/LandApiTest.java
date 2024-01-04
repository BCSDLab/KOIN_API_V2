package in.koreatech.koin.acceptance;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.domain.land.repository.LandRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class LandApiTest extends AcceptanceTest {

    @Autowired
    private LandRepository landRepository;

    @Test
    @DisplayName("복덕방 리스트를 조회한다.")
    void getLands() {
        Land request = Land.builder()
            .internalName("복덕방")
            .name("복덕방")
            .roomType("원룸")
            .latitude("37.555")
            .longitude("126.555")
            .monthlyFee("100")
            .charterFee("1000")
            .build();

        Land land = landRepository.save(request);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .get("/lands")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getList(".").size()).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getLong("[0].id")).isEqualTo(land.getId());
                softly.assertThat(response.body().jsonPath().getString("[0].internal_name"))
                    .isEqualTo(land.getInternalName());
                softly.assertThat(response.body().jsonPath().getString("[0].name")).isEqualTo(land.getName());
                softly.assertThat(response.body().jsonPath().getString("[0].room_type"))
                    .isEqualTo(land.getRoomType());
                softly.assertThat(response.body().jsonPath().getString("[0].latitude"))
                    .isEqualTo(land.getLatitude());
                softly.assertThat(response.body().jsonPath().getString("[0].longitude"))
                    .isEqualTo(land.getLongitude());
                softly.assertThat(response.body().jsonPath().getString("[0].monthly_fee"))
                    .isEqualTo(land.getMonthlyFee());
                softly.assertThat(response.body().jsonPath().getString("[0].charter_fee"))
                    .isEqualTo(land.getCharterFee());
            }
        );
    }
}
