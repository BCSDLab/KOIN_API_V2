package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.LandAcceptanceFixture;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.domain.land.repository.LandRepository;

class LandApiTest extends AcceptanceTest {

    @Autowired
    private LandAcceptanceFixture landFixture;

    @Autowired
    private LandRepository landRepository;

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
                            "monthly_fee": null,
                            "latitude": 37.555,
                            "charter_fee": null,
                            "name": "신안빌",
                            "address": "서울시 강남구",
                            "is_monthly_available": true,
                            "is_charter_available": true,
                            "id": 1,
                            "longitude": 126.555,
                            "room_type": "원룸"
                        },
                        {
                            "internal_name": "에",
                            "monthly_fee": null,
                            "latitude": 37.555,
                            "charter_fee": null,
                            "name": "에듀윌",
                            "address": "천안시 동남구 강남구",
                            "is_monthly_available": true,
                            "is_charter_available": true,
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
                    "monthly_fee": null,
                    "opt_elevator": false,
                    "opt_water_purifier": false,
                    "opt_washer": false,
                    "latitude": 37.555,
                    "charter_fee": null,
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
                    "floor": null,
                    "management_fee": null,
                    "opt_desk": false,
                    "opt_closet": false,
                    "longitude": 126.555,
                    "address": "천안시 동남구 강남구",
                    "opt_bed": false,
                    "size": "100.0",
                    "phone": "010-1133-5555",
                    "opt_air_conditioner": false,
                    "name": "에듀윌",
                    "deposit": null,
                    "opt_microwave": false,
                    "permalink": "%EC%97%90",
                    "room_type": "원룸"
                }
                """));
    }

    @Test
    void 복덕방_상세에서_매물_정보는_비우고_DB_값은_유지한다() throws Exception {
        Land land = landRepository.save(
            Land.builder()
                .internalName("매물빌")
                .name("매물빌")
                .latitude("36.766")
                .longitude("127.284")
                .address("충청남도 천안시 동남구 병천면")
                .description("1년 계약시 20만원 할인")
                .floor(3)
                .monthlyFee("30")
                .optRefrigerator(true)
                .optElevator(true)
                .build()
        );

        mockMvc.perform(
                get("/lands/{id}", land.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value(nullValue()))
            .andExpect(jsonPath("$.floor").value(nullValue()))
            .andExpect(jsonPath("$.monthly_fee").value(nullValue()))
            .andExpect(jsonPath("$.opt_refrigerator").value(false))
            .andExpect(jsonPath("$.opt_elevator").value(false))
            .andExpect(jsonPath("$.address").value("충청남도 천안시 동남구 병천면"));

        Land savedLand = landRepository.getById(land.getId());
        assertSoftly(softly -> {
            softly.assertThat(savedLand.getDescription()).isEqualTo("1년 계약시 20만원 할인");
            softly.assertThat(savedLand.getMonthlyFee()).isEqualTo("30");
            softly.assertThat(savedLand.isOptRefrigerator()).isTrue();
        });
    }

    @Test
    void 방_크기가_없으면_상세에서_null로_응답한다() throws Exception {
        Land land = landRepository.save(
            Land.builder()
                .internalName("크기없음빌")
                .name("크기없음빌")
                .latitude("36.766")
                .longitude("127.284")
                .build()
        );

        mockMvc.perform(
                get("/lands/{id}", land.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(nullValue()));
    }

    @Test
    void 좌표가_숫자가_아닌_건물이_있어도_목록은_조회되고_그_건물의_좌표만_null이다() throws Exception {
        landFixture.신안빌();
        landRepository.save(
            Land.builder()
                .internalName("좌표오류빌")
                .name("좌표오류빌")
                .latitude("")
                .longitude("abc")
                .size("넓음")
                .build()
        );

        mockMvc.perform(
                get("/lands")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lands.length()").value(2))
            .andExpect(jsonPath("$.lands[0].latitude").value(37.555))
            .andExpect(jsonPath("$.lands[1].latitude").value(nullValue()))
            .andExpect(jsonPath("$.lands[1].longitude").value(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "Infinity", "-Infinity", "1e309", "넓음", " "})
    void 숫자가_아니거나_유한하지_않은_좌표와_크기는_상세에서_null로_응답한다(String invalid) throws Exception {
        Land land = landRepository.save(
            Land.builder()
                .internalName("비정상값빌" + invalid)
                .name("비정상값빌" + invalid)
                .latitude(invalid)
                .longitude("127.284")
                .size(invalid)
                .build()
        );

        mockMvc.perform(
                get("/lands/{id}", land.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.latitude").value(nullValue()))
            .andExpect(jsonPath("$.longitude").value(127.284))
            .andExpect(jsonPath("$.size").value(nullValue()));
    }

    @Test
    void 복덕방_목록에서_월세_전세_가능_여부만_알려준다() throws Exception {
        landRepository.save(
            Land.builder()
                .internalName("월세만빌")
                .name("월세만빌")
                .latitude("36.766")
                .longitude("127.284")
                .monthlyFee("30")
                .charterFee(" ")
                .build()
        );

        mockMvc.perform(
                get("/lands")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lands[0].monthly_fee").value(nullValue()))
            .andExpect(jsonPath("$.lands[0].charter_fee").value(nullValue()))
            .andExpect(jsonPath("$.lands[0].is_monthly_available").value(true))
            .andExpect(jsonPath("$.lands[0].is_charter_available").value(false));
    }
}
