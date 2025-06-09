package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.acceptance.fixture.LandAcceptanceFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LandApiTest extends AcceptanceTest {

    @Autowired
    private LandAcceptanceFixture landFixture;

    @Test
    void 복덕방_리스트를_조회한다() throws Exception {
        landFixture.신안빌();
        landFixture.에듀윌();

        mockMvc.perform(
                get("/lands")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }

    @Test
    void 복덕방을_단일_조회한다() throws Exception {
        Land land = landFixture.에듀윌();

        mockMvc.perform(
                get("/lands/{id}", land.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                """));
    }
}
