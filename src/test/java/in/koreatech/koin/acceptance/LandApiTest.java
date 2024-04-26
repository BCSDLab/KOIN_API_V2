package in.koreatech.koin.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.fixture.LandFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
class LandApiTest extends AcceptanceTest {

    @Autowired
    private LandFixture landFixture;

    @Test
    @DisplayName("복덕방 리스트를 조회한다.")
    void getLands() {
        landFixture.신안빌();
        landFixture.에듀윌();

        var response = RestAssured
            .given()
            .when()
            .get("/lands")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "lands": [
                        {
                            "internal_name": "신",
                            "monthly_fee": "100",
                            "latitude": 37.555,
                            "charter_fee": "1000",
                            "name": "신안빌",
                            "id": 1,
                            "longitude": 126.555,
                            "room_type": "원룸"
                        },
                        {
                            "internal_name": "에",
                            "monthly_fee": "100",
                            "latitude": 37.555,
                            "charter_fee": "1000",
                            "name": "에듀윌",
                            "id": 2,
                            "longitude": 126.555,
                            "room_type": "원룸"
                        }
                    ]
                }
                    """);
    }

    @Test
    @DisplayName("복덕방을 단일 조회한다.")
    void getLand() {
        Land land = landFixture.에듀윌();

        var response = RestAssured
            .given()
            .when()
            .get("/lands/{id}", land.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "opt_electronic_door_locks": false,
                    "opt_tv": false,
                    "monthly_fee": "100",
                    "opt_elevator": false,
                    "opt_water_purifier": false,
                    "opt_washer": false,
                    "latitude": 37.555,
                    "charter_fee": "1000",
                    "opt_veranda": false,
                    "created_at": "2024-01-15 12:00:00",
                    "description": null,
                    "image_urls": [
                        "https://example1.test.com/image.jpeg",
                        "https://example2.test.com/image.jpeg"
                    ],
                    "opt_gas_range": false,
                    "opt_induction": false,
                    "internal_name": "에",
                    "is_deleted": false,
                    "updated_at": "2024-01-15 12:00:00",
                    "opt_bidet": false,
                    "opt_shoe_closet": false,
                    "opt_refrigerator": false,
                    "id": 1,
                    "floor": 1,
                    "management_fee": "100",
                    "opt_desk": false,
                    "opt_closet": false,
                    "longitude": 126.555,
                    "address": "천안시 동남구 강남구",
                    "opt_bed": false,
                    "size": "100.0",
                    "phone": "010-1133-5555",
                    "opt_air_conditioner": false,
                    "name": "에듀윌",
                    "deposit": "1000",
                    "opt_microwave": false,
                    "permalink": "%EC%97%90",
                    "room_type": "원룸"
                }
                """);
    }
}
