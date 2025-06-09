package in.koreatech.koin.acceptance.admin;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.acceptance.fixture.LandFixture;
import in.koreatech.koin.acceptance.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AdminLandApiTest extends AcceptanceTest {

    @Autowired
    private AdminLandRepository adminLandRepository;

    @Autowired
    private LandFixture landFixture;

    @Autowired
    private UserFixture userFixture;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 관리자_권한으로_복덕방_목록을_검색한다() throws Exception {
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

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/lands")
                    .header("Authorization", "Bearer " + token)
                    .param("page", "1")
                    .param("is_deleted", "false")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(11))
            .andExpect(jsonPath("$.current_count").value(10))
            .andExpect(jsonPath("$.total_page").value(2))
            .andExpect(jsonPath("$.current_page").value(1))
            .andExpect(jsonPath("$.lands.length()").value(10));
    }

    @Test
    void 관리자_권한으로_복덕방을_추가한다() throws Exception {
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

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                post("/admin/lands")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
            )
            .andExpect(status().isCreated());

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
    void 관리자_권한으로_복덕방을_삭제한다() throws Exception {
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

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                delete("/admin/lands/{id}", landId)
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk());

        Land deletedLand = adminLandRepository.getById(landId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deletedLand.getName()).isEqualTo("금실타운");
            softly.assertThat(deletedLand.isDeleted()).isEqualTo(true);
        });
    }

    @Test
    void 관리자의_권한으로_특정_복덕방_정보를_조회한다() throws Exception {
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

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/lands/{id}", landId)
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
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
                """, landId)));
    }

    @Test
    void 관리자_권한으로_복덕방_정보를_수정한다() throws Exception {
        Land land = landFixture.신안빌();
        Integer landId = land.getId();

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

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

        mockMvc.perform(
                put("/admin/lands/{id}", landId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
            )
            .andExpect(status().isOk());

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
    void 관리자_권한으로_복덕방_삭제를_취소한다() throws Exception {
        Land deletedLand = landFixture.삭제된_복덕방();
        Integer landId = deletedLand.getId();

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                post("/admin/lands/{id}/undelete", landId)
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk());

        Land undeletedLand = adminLandRepository.getById(landId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(undeletedLand).isNotNull();
            softly.assertThat(undeletedLand.getName()).isEqualTo("삭제된 복덕방");
            softly.assertThat(undeletedLand.isDeleted()).isFalse();
        });
    }
}
