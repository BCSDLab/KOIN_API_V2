package in.koreatech.koin.acceptance.admin;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.LandAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.land.repository.AdminLandRepository;
import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.domain.land.model.Land;
import jakarta.persistence.EntityManager;

class AdminLandApiTest extends AcceptanceTest {

    @Autowired
    private AdminLandRepository adminLandRepository;

    @Autowired
    private LandAcceptanceFixture landFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void 관리자_권한으로_복덕방_목록에서_주소_연락처_수정일을_조회한다() throws Exception {
        adminLandRepository.save(
            Land.builder()
                .internalName("연락처빌")
                .name("연락처빌")
                .address("충청남도 천안시 동남구 병천면")
                .phone("041-111-1111")
                .build()
        );
        adminLandRepository.save(
            Land.builder()
                .internalName("무연락빌")
                .name("무연락빌")
                .address("충청남도 천안시 동남구 병천면 가전리")
                .build()
        );
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/lands")
                    .header("Authorization", "Bearer " + token)
                    .param("page", "1")
                    .param("is_deleted", "false")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lands[0].address").value("충청남도 천안시 동남구 병천면"))
            .andExpect(jsonPath("$.lands[0].phone").value("041-111-1111"))
            .andExpect(jsonPath("$.lands[0].updated_at").value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
            .andExpect(jsonPath("$.lands[1].address").value("충청남도 천안시 동남구 병천면 가전리"))
            .andExpect(jsonPath("$.lands[1].phone").value(nullValue()));
    }

    @Test
    void 관리자_권한으로_삭제된_복덕방_목록에서도_주소_연락처_수정일을_조회한다() throws Exception {
        adminLandRepository.save(
            Land.builder()
                .internalName("삭제연락처빌")
                .name("삭제연락처빌")
                .address("충청남도 천안시 동남구 병천면")
                .phone("041-111-1111")
                .isDeleted(true)
                .build()
        );
        adminLandRepository.save(
            Land.builder()
                .internalName("일반연락처빌")
                .name("일반연락처빌")
                .address("충청남도 천안시 동남구 병천면 가전리")
                .build()
        );
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/lands")
                    .header("Authorization", "Bearer " + token)
                    .param("page", "1")
                    .param("is_deleted", "true")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lands.length()").value(1))
            .andExpect(jsonPath("$.lands[0].address").value("충청남도 천안시 동남구 병천면"))
            .andExpect(jsonPath("$.lands[0].phone").value("041-111-1111"))
            .andExpect(jsonPath("$.lands[0].updated_at").value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")));
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
            softly.assertThat(updatedLand.getInternalName()).isEqualTo("신안빌수정");
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

    @Test
    void 복덕방을_수정할_때_숨긴_항목을_보내지_않으면_기존_값을_유지한다() throws Exception {
        Land land = adminLandRepository.save(
            Land.builder()
                .internalName("보존빌")
                .name("보존빌")
                .roomType("원룸")
                .latitude("36.766")
                .longitude("127.284")
                .address("충청남도 천안시 동남구 병천면")
                .phone("041-111-1111")
                .description("1년 계약시 20만원 할인")
                .floor(3)
                .deposit("300")
                .monthlyFee("30")
                .charterFee("5000")
                .managementFee("5")
                .optRefrigerator(true)
                .optWasher(true)
                .build()
        );
        Map<String, Object> request = 필수값만_있는_요청("보존빌");
        request.put("phone", "041-222-2222");

        수정한다(land.getId(), request).andExpect(status().isOk());

        Land updatedLand = adminLandRepository.getById(land.getId());
        assertSoftly(softly -> {
            softly.assertThat(updatedLand.getPhone()).isEqualTo("041-222-2222");
            softly.assertThat(updatedLand.getDescription()).isEqualTo("1년 계약시 20만원 할인");
            softly.assertThat(updatedLand.getFloor()).isEqualTo(3);
            softly.assertThat(updatedLand.getDeposit()).isEqualTo("300");
            softly.assertThat(updatedLand.getMonthlyFee()).isEqualTo("30");
            softly.assertThat(updatedLand.getCharterFee()).isEqualTo("5000");
            softly.assertThat(updatedLand.getManagementFee()).isEqualTo("5");
            softly.assertThat(updatedLand.isOptRefrigerator()).isTrue();
            softly.assertThat(updatedLand.isOptWasher()).isTrue();
            softly.assertThat(updatedLand.isOptTv()).isFalse();
        });
    }

    @Test
    void 복덕방을_수정할_때_보이는_항목을_비워_보내면_비운다() throws Exception {
        Land land = adminLandRepository.save(
            Land.builder()
                .internalName("비움빌")
                .name("비움빌")
                .roomType("원룸")
                .size("8.0")
                .latitude("36.766")
                .longitude("127.284")
                .address("충청남도 천안시 동남구 병천면")
                .phone("041-111-1111")
                .imageUrls(List.of("https://example.test.com/a.jpeg"))
                .build()
        );
        Map<String, Object> request = 필수값만_있는_요청("비움빌");
        request.put("phone", null);
        request.put("image_urls", List.of());

        수정한다(land.getId(), request).andExpect(status().isOk());

        Land updatedLand = adminLandRepository.getById(land.getId());
        assertSoftly(softly -> {
            softly.assertThat(updatedLand.getPhone()).isNull();
            softly.assertThat(updatedLand.getImageUrls()).isNull();
            softly.assertThat(updatedLand.getSize()).isNull();
            softly.assertThat(updatedLand.getRoomType()).isNull();
        });
    }

    @Test
    void 복덕방을_추가할_때_월세_전세_가능_여부를_저장하고_보내지_않으면_false로_저장한다() throws Exception {
        Map<String, Object> request = 필수값만_있는_요청("매물여부추가빌");
        request.put("is_monthly_available", true);

        추가한다(request).andExpect(status().isCreated());

        Land savedLand = adminLandRepository.getByName("매물여부추가빌");
        assertSoftly(softly -> {
            softly.assertThat(savedLand.isMonthlyAvailable()).isTrue();
            softly.assertThat(savedLand.isCharterAvailable()).isFalse();
        });
    }

    @Test
    void 복덕방을_수정할_때_월세_전세_가능_여부는_보낸_값만_바꾸고_보내지_않으면_유지한다() throws Exception {
        Land land = adminLandRepository.save(
            Land.builder()
                .internalName("매물여부수정빌")
                .name("매물여부수정빌")
                .latitude("36.766")
                .longitude("127.284")
                .address("충청남도 천안시 동남구 병천면")
                .isMonthlyAvailable(true)
                .isCharterAvailable(true)
                .build()
        );
        Map<String, Object> request = 필수값만_있는_요청("매물여부수정빌");
        request.put("is_charter_available", false);

        수정한다(land.getId(), request).andExpect(status().isOk());

        Land updatedLand = adminLandRepository.getById(land.getId());
        assertSoftly(softly -> {
            softly.assertThat(updatedLand.isMonthlyAvailable()).isTrue();
            softly.assertThat(updatedLand.isCharterAvailable()).isFalse();
        });
    }

    @Test
    void 어드민_상세에서_월세_전세_가능_여부를_조회한다() throws Exception {
        Land land = adminLandRepository.save(
            Land.builder()
                .internalName("매물여부상세빌")
                .name("매물여부상세빌")
                .latitude("36.766")
                .longitude("127.284")
                .isMonthlyAvailable(false)
                .isCharterAvailable(true)
                .build()
        );

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/lands/{id}", land.getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.is_monthly_available").value(false))
            .andExpect(jsonPath("$.is_charter_available").value(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "address", "latitude", "longitude"})
    void 복덕방을_추가할_때_필수값이_없으면_400을_응답한다(String missingField) throws Exception {
        Map<String, Object> request = 필수값만_있는_요청("필수값추가빌" + missingField);
        request.remove(missingField);

        추가한다(request).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "address", "latitude", "longitude"})
    void 복덕방을_수정할_때_필수값이_없으면_400을_응답하고_값을_바꾸지_않는다(String missingField) throws Exception {
        Land land = landFixture.신안빌();
        Map<String, Object> request = 필수값만_있는_요청("신안빌 수정");
        request.remove(missingField);

        수정한다(land.getId(), request).andExpect(status().isBadRequest());

        Land unchangedLand = adminLandRepository.getById(land.getId());
        assertSoftly(softly -> {
            softly.assertThat(unchangedLand.getName()).isEqualTo("신안빌");
            softly.assertThat(unchangedLand.getLatitude()).isEqualTo(37.555);
            softly.assertThat(unchangedLand.getLongitude()).isEqualTo(126.555);
        });
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "0, 127.284", "36.766, 0", "40.0, 127.284", "36.766, 120.0"})
    void 복덕방_좌표가_국내_범위를_벗어나면_400을_응답한다(double latitude, double longitude) throws Exception {
        Map<String, Object> request = 필수값만_있는_요청("좌표빌" + latitude + longitude);
        request.put("latitude", latitude);
        request.put("longitude", longitude);

        추가한다(request).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 사진_목록에_비어_있는_주소가_있으면_400을_응답한다(String imageUrl) throws Exception {
        Map<String, Object> request = 필수값만_있는_요청("빈사진빌");
        request.put("image_urls", Arrays.asList("https://example.test.com/a.jpeg", imageUrl));

        추가한다(request).andExpect(status().isBadRequest());
    }

    @Test
    void 사진_주소에_쉼표가_있어도_어드민_상세에서_한_장으로_조회한다() throws Exception {
        Land land = adminLandRepository.save(
            Land.builder()
                .internalName("쉼표빌")
                .name("쉼표빌")
                .latitude("36.766")
                .longitude("127.284")
                .address("충청남도 천안시 동남구 병천면")
                .imageUrls(List.of("https://example.test.com/a,b.jpeg", "https://example.test.com/\"c\".jpeg"))
                .build()
        );
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/lands/{id}", land.getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.image_urls.length()").value(2))
            .andExpect(jsonPath("$.image_urls[0]").value("https://example.test.com/a,b.jpeg"))
            .andExpect(jsonPath("$.image_urls[1]").value("https://example.test.com/\"c\".jpeg"));
    }

    @Test
    void 예전_방식으로_저장된_사진_문자열도_어드민_상세에서_조회한다() throws Exception {
        Land land = adminLandRepository.save(
            Land.builder()
                .internalName("예전사진빌")
                .name("예전사진빌")
                .latitude("36.766")
                .longitude("127.284")
                .address("충청남도 천안시 동남구 병천면")
                .build()
        );
        entityManager.flush();
        entityManager.createNativeQuery("UPDATE lands SET image_urls = :imageUrls WHERE id = :id")
            .setParameter("imageUrls", "[\"https://example.test.com/a\"b.jpeg\",\"https://example.test.com/c.jpeg\"]")
            .setParameter("id", land.getId())
            .executeUpdate();
        entityManager.clear();
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/lands/{id}", land.getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.image_urls.length()").value(2))
            .andExpect(jsonPath("$.image_urls[1]").value("https://example.test.com/c.jpeg"));
    }

    private Map<String, Object> 필수값만_있는_요청(String name) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("address", "충청남도 천안시 동남구 병천면");
        request.put("latitude", 36.766);
        request.put("longitude", 127.284);
        return request;
    }

    private ResultActions 추가한다(Map<String, Object> request) throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());
        return mockMvc.perform(
            post("/admin/lands")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );
    }

    private ResultActions 수정한다(Integer landId, Map<String, Object> request) throws Exception {
        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());
        return mockMvc.perform(
            put("/admin/lands/{id}", landId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );
    }

    @Test
    void 관리자가_복덕방을_추가할_때_이름의_모든_공백을_빼고_내부_이름을_저장한다() throws Exception {
        String jsonBody = """
            {
                "name": "별밭\\t하우스\\u3000B동",
                "room_type": "원룸",
                "latitude": "36.765",
                "longitude": "127.282",
                "address": "충청남도 천안시 동남구 병천면"
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

        Land savedLand = adminLandRepository.getByName("별밭\t하우스\u3000B동");
        assertSoftly(softly -> softly.assertThat(savedLand.getInternalName()).isEqualTo("별밭하우스b동"));
    }

    @Test
    void 관리자가_복덕방을_수정할_때_이름의_모든_공백을_빼고_내부_이름을_저장한다() throws Exception {
        Land land = landFixture.신안빌();

        String jsonBody = """
            {
                "name": "신안빌\\u00A0수정\\n",
                "room_type": "원룸",
                "latitude": "36.765",
                "longitude": "127.282",
                "address": "충청남도 천안시 동남구 병천면"
            }
            """;

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                put("/admin/lands/{id}", land.getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
            )
            .andExpect(status().isOk());

        Land updatedLand = adminLandRepository.getById(land.getId());
        assertSoftly(softly -> softly.assertThat(updatedLand.getInternalName()).isEqualTo("신안빌수정"));
    }
}
