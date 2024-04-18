package in.koreatech.koin.acceptance;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.domain.land.repository.LandRepository;
import io.restassured.RestAssured;

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
        Land request2 = Land.builder()
            .internalName("복덕방2")
            .name("복덕방2")
            .roomType("원룸2")
            .latitude("37.5552")
            .longitude("126.5552")
            .monthlyFee("102")
            .charterFee("1002")
            .build();

        Land land = landRepository.save(request);
        Land land2 = landRepository.save(request2);

        var response = RestAssured
            .given()
            .when()
            .get("/lands")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getList("lands").size()).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getInt("lands[0].id")).isEqualTo(land.getId());
                softly.assertThat(response.body().jsonPath().getString("lands[0].internal_name"))
                    .isEqualTo(land.getInternalName());
                softly.assertThat(response.body().jsonPath().getString("lands[0].name")).isEqualTo(land.getName());
                softly.assertThat(response.body().jsonPath().getString("lands[0].room_type"))
                    .isEqualTo(land.getRoomType());
                softly.assertThat(response.body().jsonPath().getString("lands[0].latitude"))
                    .isEqualTo(land.getLatitude());
                softly.assertThat(response.body().jsonPath().getString("lands[0].longitude"))
                    .isEqualTo(land.getLongitude());
                softly.assertThat(response.body().jsonPath().getString("lands[0].monthly_fee"))
                    .isEqualTo(land.getMonthlyFee());
                softly.assertThat(response.body().jsonPath().getString("lands[0].charter_fee"))
                    .isEqualTo(land.getCharterFee());

                softly.assertThat(response.body().jsonPath().getInt("lands[1].id")).isEqualTo(land2.getId());
                softly.assertThat(response.body().jsonPath().getString("lands[1].internal_name"))
                    .isEqualTo(land2.getInternalName());
                softly.assertThat(response.body().jsonPath().getString("lands[1].name")).isEqualTo(land2.getName());
                softly.assertThat(response.body().jsonPath().getString("lands[1].room_type"))
                    .isEqualTo(land2.getRoomType());
                softly.assertThat(response.body().jsonPath().getString("lands[1].latitude"))
                    .isEqualTo(land2.getLatitude());
                softly.assertThat(response.body().jsonPath().getString("lands[1].longitude"))
                    .isEqualTo(land2.getLongitude());
                softly.assertThat(response.body().jsonPath().getString("lands[1].monthly_fee"))
                    .isEqualTo(land2.getMonthlyFee());
                softly.assertThat(response.body().jsonPath().getString("lands[1].charter_fee"))
                    .isEqualTo(land2.getCharterFee());
            }
        );
    }

    @Test
    @DisplayName("복덕방을 단일 조회한다.")
    void getLand() {
        Land request = Land.builder()
            .internalName("복덕방")
            .name("복덕방")
            .roomType("원룸")
            .latitude("37.555")
            .longitude("126.555")
            .floor(1)
            .monthlyFee("100")
            .charterFee("1000")
            .deposit("1000")
            .managementFee("100")
            .phone("010-1234-5678")
            .address("서울시 강남구")
            .size("100.0")
            .imageUrls("""
                ["https://example1.test.com/image.jpeg",
                "https://example2.test.com/image.jpeg"]
                """)
            .build();

        Land land = landRepository.save(request);

        var response = RestAssured
            .given()
            .when()
            .get("/lands/{id}", land.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getBoolean("opt_electronic_door_locks"))
                    .isEqualTo(land.isOptElectronicDoorLocks());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_tv")).isEqualTo(land.isOptTv());
                softly.assertThat(response.body().jsonPath().getString("monthly_fee"))
                    .isEqualTo(land.getMonthlyFee());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_elevator"))
                    .isEqualTo(land.isOptElevator());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_water_purifier"))
                    .isEqualTo(land.isOptWaterPurifier());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_washer"))
                    .isEqualTo(land.isOptWasher());
                softly.assertThat(response.body().jsonPath().getString("latitude"))
                    .isEqualTo(land.getLatitude());
                softly.assertThat(response.body().jsonPath().getString("charter_fee"))
                    .isEqualTo(land.getCharterFee());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_veranda"))
                    .isEqualTo(land.isOptVeranda());
                softly.assertThat(response.body().jsonPath().getString("created_at"))
                    .contains(land.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                softly.assertThat(response.body().jsonPath().getString("description"))
                    .isEqualTo(land.getDescription());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_gas_range"))
                    .isEqualTo(land.isOptGasRange());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_induction"))
                    .isEqualTo(land.isOptInduction());
                softly.assertThat(response.body().jsonPath().getString("internal_name"))
                    .isEqualTo(land.getInternalName());
                softly.assertThat(response.body().jsonPath().getBoolean("is_deleted"))
                    .isEqualTo(land.isDeleted());
                softly.assertThat(response.body().jsonPath().getString("updated_at"))
                    .contains(land.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                softly.assertThat(response.body().jsonPath().getBoolean("opt_bidet"))
                    .isEqualTo(land.isOptBidet());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_shoe_closet"))
                    .isEqualTo(land.isOptShoeCloset());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_refrigerator"))
                    .isEqualTo(land.isOptRefrigerator());
                softly.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(land.getId());
                softly.assertThat(response.body().jsonPath().getInt("floor")).isEqualTo(land.getFloor());
                softly.assertThat(response.body().jsonPath().getString("management_fee"))
                    .isEqualTo(land.getManagementFee());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_desk"))
                    .isEqualTo(land.isOptDesk());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_closet"))
                    .isEqualTo(land.isOptCloset());
                softly.assertThat(response.body().jsonPath().getString("longitude"))
                    .isEqualTo(land.getLongitude());
                softly.assertThat(response.body().jsonPath().getString("address"))
                    .isEqualTo(land.getAddress());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_bed"))
                    .isEqualTo(land.isOptBed());
                softly.assertThat(response.body().jsonPath().getString("size"))
                    .isEqualTo(land.getSize());
                softly.assertThat(response.body().jsonPath().getString("phone"))
                    .isEqualTo(land.getPhone());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_air_conditioner"))
                    .isEqualTo(land.isOptAirConditioner());
                softly.assertThat(response.body().jsonPath().getString("name"))
                    .isEqualTo(land.getName());
                softly.assertThat(response.body().jsonPath().getString("deposit"))
                    .isEqualTo(land.getDeposit());
                softly.assertThat(response.body().jsonPath().getBoolean("opt_microwave"))
                    .isEqualTo(land.isOptMicrowave());
                softly.assertThat(response.body().jsonPath().getString("permalink"))
                    .isEqualTo(URLEncoder.encode(land.getInternalName(), StandardCharsets.UTF_8));
                softly.assertThat(response.body().jsonPath().getString("room_type"))
                    .isEqualTo(land.getRoomType());
                softly.assertThat(response.body().jsonPath().getList("image_urls")).hasSize(2);
            }
        );
    }
}
