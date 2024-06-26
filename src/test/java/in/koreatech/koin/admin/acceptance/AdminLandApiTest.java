package in.koreatech.koin.admin.acceptance;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.LandFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
class AdminLandApiTest extends AcceptanceTest {

    @Autowired
    private AdminLandRepository adminLandRepository;

    @Autowired
    private LandFixture landFixture;

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

    @Test
    @DisplayName("관리자의 권한으로 특정 복덕방 정보를 조회한다.")
    void getLand() {
        // 복덕방 생성
        Land request = Land.builder()
            .internalName("금실타운")
            .name("금실타운")
            .roomType("원룸")
            .latitude("37.555")
            .longitude("126.555")
            .size("9.0")
            .monthlyFee("100")
            .charterFee("1000")
            .address("가전리 123")
            .description("테스트용 복덕방")
            .build();

        Land savedLand = adminLandRepository.save(request);
        Integer landId = savedLand.getId();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/admin/lands/{id}", landId)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
            {
                "id": %d,
                "name": "금실타운",
                "internal_name": "금실타운",
                "size": 9.0,
                "room_type": "원룸",
                "latitude": 37.555,
                "longitude": 126.555,
                "phone": null,
                "image_urls": [],
                "address": "가전리 123",
                "description": "테스트용 복덕방",
                "floor": null,
                "deposit": null,
                "monthly_fee": "100",
                "charter_fee": "1000",
                "management_fee": null,
                "opt_closet": false,
                "opt_tv": false,
                "opt_microwave": false,
                "opt_gas_range": false,
                "opt_induction": false,
                "opt_water_purifier": false,
                "opt_air_conditioner": false,
                "opt_washer": false,
                "opt_bed": false,
                "opt_bidet": false,
                "opt_desk": false,
                "opt_electronic_door_locks": false,
                "opt_elevator": false,
                "opt_refrigerator": false,
                "opt_shoe_closet": false,
                "opt_veranda": false,
                "is_deleted": false
            }
            """, landId));
    }

    @Test
    @DisplayName("관리자 권한으로 복덕방 정보를 수정한다.")
    void updateLand() {
        Land land = landFixture.신안빌();
        Integer landId = land.getId();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        String jsonBody = """
            {
                "name": "신안빌 수정",
                "internal_name": "신안빌",
                "size": "110.0",
                "room_type": "투룸",
                "latitude": "37.556",
                "longitude": "126.556",
                "phone": "010-1234-5679",
                "image_urls": ["http://newimage1.com", "http://newimage2.com"],
                "address": "서울시 강남구 신사동",
                "description": "신안빌 수정 설명",
                "floor": 5,
                "deposit": "50",
                "monthly_fee": "150만원",
                "charter_fee": "5000",
                "management_fee": "150",
                "opt_closet": true,
                "opt_tv": false,
                "opt_microwave": true,
                "opt_gas_range": false,
                "opt_induction": true,
                "opt_water_purifier": false,
                "opt_air_conditioner": true,
                "opt_washer": true
            }
            """;

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body(jsonBody)
            .when()
            .put("/admin/lands/{id}", landId)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Land updatedLand = adminLandRepository.getById(landId);

        assertSoftly(softly -> {
            softly.assertThat(updatedLand.getName()).isEqualTo("신안빌 수정");
            softly.assertThat(updatedLand.getInternalName()).isEqualTo("신안빌");
            softly.assertThat(updatedLand.getSize()).isEqualTo(110.0);
            softly.assertThat(updatedLand.getRoomType()).isEqualTo("투룸");
            softly.assertThat(updatedLand.getLatitude()).isEqualTo(37.556);
            softly.assertThat(updatedLand.getLongitude()).isEqualTo(126.556);
            softly.assertThat(updatedLand.getPhone()).isEqualTo("010-1234-5679");
            softly.assertThat(updatedLand.getImageUrls()).containsAnyOf("http://newimage1.com", "http://newimage2.com");
            softly.assertThat(updatedLand.getAddress()).isEqualTo("서울시 강남구 신사동");
            softly.assertThat(updatedLand.getDescription()).isEqualTo("신안빌 수정 설명");
            softly.assertThat(updatedLand.getFloor()).isEqualTo(5);
            softly.assertThat(updatedLand.getDeposit()).isEqualTo("50");
            softly.assertThat(updatedLand.getMonthlyFee()).isEqualTo("150만원");
            softly.assertThat(updatedLand.getCharterFee()).isEqualTo("5000");
            softly.assertThat(updatedLand.getManagementFee()).isEqualTo("150");
            softly.assertThat(updatedLand.isOptCloset()).isTrue();
            softly.assertThat(updatedLand.isOptTv()).isFalse();
            softly.assertThat(updatedLand.isOptMicrowave()).isTrue();
            softly.assertThat(updatedLand.isOptGasRange()).isFalse();
            softly.assertThat(updatedLand.isOptInduction()).isTrue();
            softly.assertThat(updatedLand.isOptWaterPurifier()).isFalse();
            softly.assertThat(updatedLand.isOptAirConditioner()).isTrue();
            softly.assertThat(updatedLand.isOptWasher()).isTrue();
            softly.assertThat(updatedLand.isDeleted()).isEqualTo(false);
        });
    }

    @Test
    @DisplayName("관리자 권한으로 복덕방 삭제를 취소한다.")
    void undeleteLand() {
        Land deletedLand = landFixture.삭제된_복덕방();
        Integer landId = deletedLand.getId();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .post("/admin/lands/{id}/undelete", landId)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Land undeletedLand = adminLandRepository.getById(landId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(undeletedLand).isNotNull();
            softly.assertThat(undeletedLand.getName()).isEqualTo("삭제된 복덕방");
            softly.assertThat(undeletedLand.isDeleted()).isFalse();
        });
    }

}
