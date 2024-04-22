package in.koreatech.koin.admin;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.domain.land.model.Land;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class AdminLandApiTest extends AcceptanceTest {

    @Autowired
    private AdminLandRepository adminLandRepository;

    @Test
    @DisplayName("관리자 권한으로 복덕방 목록을 검색한다.")
    void getLands() {
        for (int i = 0; i < 11; i++) {
            Land request = Land.builder()
                .internalName("복덕방" + i)
                .name("복덕방" + i)
                .roomType("원룸")
                .latitude("37.555")
                .longitude("126.555")
                .monthlyFee("100")
                .charterFee("1000")
                .build();

            adminLandRepository.save(request);
        }

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("page", 1)
            .param("is_deleted", false)
            .get("/admin/lands")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getInt("total_count")).isEqualTo(11);
                softly.assertThat(response.body().jsonPath().getInt("current_count")).isEqualTo(10);
                softly.assertThat(response.body().jsonPath().getInt("total_page")).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getInt("current_page")).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getList("lands").size()).isEqualTo(10);
            }
        );
    }
}
