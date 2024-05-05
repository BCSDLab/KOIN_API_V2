package in.koreatech.koin.admin.acceptance;

import static in.koreatech.koin.support.JsonAssertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.land.dto.AdminLandsRequest;
import in.koreatech.koin.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.domain.land.model.Land;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
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

        var response = RestAssured
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

    @Test
    @DisplayName("관리자 권한으로 복덕방을 추가한다.")
    void postLands() {
        AdminLandsRequest request = new AdminLandsRequest(
            "금실타운",
            "금실타운",
            "9.0",
            "원룸",
            "37.555",
            "126.555",
            "041-111-1111",
            List.of("http://image1.com", "http://image2.com"),
            "충청남도 천안시 동남구 병천면",
            "1년 계약시 20만원 할인",
            4,
            "30",
            "200만원 (6개월)",
            "3500",
            "21(1인 기준)",
            true,
            true,
            true,
            true,
            false,
            true,
            true,
            true,
            true
        );

        RestAssured
            .given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/admin/lands")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract().asString();

        Land savedLand = adminLandRepository.findByName("금실타운");
        assertNotNull(savedLand);
        assertAll("복덕방이 잘 업데이트 되었는지 확인해본다",
            () -> assertEquals("금실타운", savedLand.getName()),
            () -> assertEquals("충청남도 천안시 동남구 병천면", savedLand.getAddress()),
            () -> assertEquals("1년 계약시 20만원 할인", savedLand.getDescription()),
            () -> assertEquals("200만원 (6개월)", savedLand.getMonthlyFee())
        );
    }
}
