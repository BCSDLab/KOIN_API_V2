package in.koreatech.koin.admin.acceptance;

import static in.koreatech.koin.support.JsonAssertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.land.dto.AdminLandsRequest;
import in.koreatech.koin.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.UserFixture;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
class AdminLandApiTest extends AcceptanceTest {

    @Autowired
    private AdminLandRepository adminLandRepository;

    @Autowired
    private UserFixture userFixture;

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

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
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
        String jsonBody = """
    {
        "name": "금실타운",
        "internal_name": "금실타운",
        "size": "9.0",
        "room_type": "원룸",
        "latitude": "37.555",
        "longitude": "126.555",
        "phone": "041-111-1111",
        "image_urls": ["http://image1.com", "http://image2.com"],
        "address": "충청남도 천안시 동남구 병천면",
        "description": "1년 계약시 20만원 할인",
        "floor": 4,
        "deposit": "30",
        "monthly_fee": "200만원 (6개월)",
        "charter_fee": "3500",
        "management_fee": "21(1인 기준)",
        "opt_closet": true,
        "opt_tv": true,
        "opt_microwave": true,
        "opt_gas_range": false,
        "opt_induction": true,
        "opt_water_purifier": true,
        "opt_air_conditioner": true,
        "opt_washer": true
    }
    """;

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body(jsonBody)
            .when()
            .post("/admin/lands")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract().asString();

        Land savedLand = adminLandRepository.getByName("금실타운");
        assertNotNull(savedLand);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(savedLand.getName()).isEqualTo("금실타운");
            softly.assertThat(savedLand.getAddress()).isEqualTo("충청남도 천안시 동남구 병천면");
            softly.assertThat(savedLand.getDescription()).isEqualTo("1년 계약시 20만원 할인");
            softly.assertThat(savedLand.getMonthlyFee()).isEqualTo("200만원 (6개월)");
            softly.assertThat(savedLand.isOptRefrigerator()).as("opt_refrigerator가 누락될 경우 false 반환여부").isEqualTo(false);
        });
    }

    @Test
    @DisplayName("관리자 권한으로 복덕방을 삭제한다.")
    void deleteLand() {
        // 복덕방 생성
        Land request = Land.builder()
            .internalName("금실타운")
            .name("금실타운")
            .roomType("원룸")
            .latitude("37.555")
            .longitude("126.555")
            .monthlyFee("100")
            .charterFee("1000")
            .build();

        Land savedLand = adminLandRepository.save(request);
        Integer landId = savedLand.getId();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/admin/lands/{id}", landId)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Land deletedLand = adminLandRepository.getById(landId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deletedLand.getName()).isEqualTo("금실타운");
            softly.assertThat(deletedLand.isDeleted()).isEqualTo(true);
        });
    }
}
