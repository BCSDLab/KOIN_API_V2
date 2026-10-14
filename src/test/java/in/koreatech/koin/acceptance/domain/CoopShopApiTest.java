package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CoopShopAcceptanceFixture;
import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.model.DayType;
import in.koreatech.koin.domain.coopshop.repository.CoopSemesterRepository;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.domain.dining.model.DiningType;

class CoopShopApiTest extends AcceptanceTest {

    @Autowired
    private CoopShopService coopShopService;

    @Autowired
    private CoopShopAcceptanceFixture coopShopFixture;

    @Autowired
    private CoopShopRepository coopShopRepository;

    @Autowired
    private CoopSemesterRepository coopSemesterRepository;

    @BeforeAll
    void setUp() {
        clear();
        coopShopFixture._23_2학기();
    }

    @Test
    void 생협의_모든_상점을_조회한다() throws Exception {
        mockMvc.perform(
                get("/coopshop")
                    .contentType(MediaType.ALL.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                        "semester": "23-2학기",
                        "from_date": "2023-09-02",
                        "to_date": "2023-12-20",
                        "updated_at" : "2024-01-15 12:00:00",
                        "coop_shops": [
                            {
                                "id": 1,
                                "name": "학생식당",
                                "opens": [
                                    {
                                        "day_of_week": "평일",
                                        "type": "아침",
                                        "open_time": "08:00",
                                        "close_time": "09:00"
                                    }
                                ],
                                "phone": "041-000-0000",
                                "location": "학생회관 1층",
                                "remarks": "공휴일 휴무"
                            },
                            {
                                "id": 2,
                                "name": "세탁소",
                                "opens": [],
                                "phone": "041-000-0000",
                                "location": "학생회관 2층",
                                "remarks": "연중무휴"
                            }
                        ]
                    }
                """));
    }

    @Test
    void 생협의_상점을_Id로_조회한다() throws Exception {
        mockMvc.perform(
                get("/coopshop/1")
                    .contentType(MediaType.ALL.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                        "id": 1,
                        "name": "학생식당",
                        "opens": [
                            {
                                "day_of_week": "평일",
                                "type": "아침",
                                "open_time": "08:00",
                                "close_time": "09:00"
                            }
                        ],
                        "phone": "041-000-0000",
                        "location": "학생회관 1층",
                        "remarks": "공휴일 휴무",
                        "updated_at" : "2024-01-15"
                    }
                """));
    }

    @Test
    void 다음_학기로_전환된다() throws Exception {
        coopShopFixture._23_겨울학기();
        coopShopService.updateSemester();
        mockMvc.perform(
                get("/coopshop")
                    .contentType(MediaType.ALL.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                        "semester": "23-겨울학기",
                        "from_date": "2023-12-21",
                        "to_date": "2024-02-28",
                        "updated_at" : "2024-01-15 12:00:00",
                        "coop_shops": [
                            {
                                "id": 3,
                                "name": "세탁소",
                                "opens": [
                                    {
                                        "day_of_week": "평일",
                                        "type": null,
                                        "open_time": "09:00",
                                        "close_time": "18:00"
                                    }
                        ],
                                "phone": "041-000-0000",
                                "location": "학생회관 2층",
                                "remarks": "연중무휴"
                            }
                        ]
                    }
                """));
    }

    @Test
    void 기존_API는_등록된_요일과_시간을_그대로_반환한다() throws Exception {
        replaceHours(hour(DayType.WEEKDAYS, "점심", "11:30", "13:30"),
            hour(DayType.SATURDAY, "점심", "12:00", "13:00"),
            hour(DayType.SUNDAY, "점심", "미운영", "미운영"));

        mockMvc.perform(get("/coopshop/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.opens[*].day_of_week").value(contains("평일", "토요일", "일요일")))
            .andExpect(jsonPath("$.opens[*].open_time").value(contains("11:30", "12:00", "미운영")));
        mockMvc.perform(get("/coopshop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.coop_shops[0].opens[*].day_of_week")
                .value(contains("평일", "토요일", "일요일")))
            .andExpect(jsonPath("$.coop_shops[0].opens[*].open_time")
                .value(contains("11:30", "12:00", "미운영")));
    }

    @Test
    void 일요일_휴무는_주말_운영시간보다_우선한다() {
        replaceHours(hour(DayType.WEEKEND, "점심", "11:30", "13:30"),
            hour(DayType.SUNDAY, "점심", "미운영", "미운영"));

        assertThat(isOpened("2026-10-18T12:00", DiningType.LUNCH, false)).isFalse();
        assertThat(isOpened("2026-10-17T12:00", DiningType.LUNCH, false)).isTrue();
    }

    @Test
    void 주말_묶음_없이_일요일_운영시간만_등록해도_판정한다() {
        replaceHours(hour(DayType.SUNDAY, "점심", "11:30", "13:30"));

        assertThat(isOpened("2026-10-18T12:00", DiningType.LUNCH, false)).isTrue();
        assertThat(isOpened("2026-10-17T12:00", DiningType.LUNCH, false)).isFalse();
    }

    @Test
    void 개별_요일_운영시간은_평일_설정보다_우선한다() {
        replaceHours(hour(DayType.WEEKDAYS, "점심", "11:30", "13:30"),
            hour(DayType.WEDNESDAY, "점심", "12:30", "13:30"));

        assertThat(isOpened("2026-10-14T12:00", DiningType.LUNCH, false)).isFalse();
        assertThat(isOpened("2026-10-14T13:00", DiningType.LUNCH, false)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "2026-10-17T10:59,true,false",
        "2026-10-17T11:00,true,true",
        "2026-10-17T11:59,false,false",
        "2026-10-17T12:00,false,true",
        "2026-10-17T13:00,false,true",
        "2026-10-17T13:01,false,false"
    })
    void 토요일_개별_시간과_사진_업로드의_한시간_여유를_적용한다(String now, boolean isMinus, boolean expected) {
        replaceHours(hour(DayType.WEEKEND, "점심", "11:30", "14:00"),
            hour(DayType.SATURDAY, "점심", "12:00", "13:00"));
        assertThat(isOpened(now, DiningType.LUNCH, isMinus)).isEqualTo(expected);
    }

    @Test
    void 개별_요일에_등록되지_않은_식사는_주말_설정으로_대체하지_않는다() {
        replaceHours(hour(DayType.WEEKEND, "저녁", "17:30", "18:30"),
            hour(DayType.SUNDAY, "점심", "11:30", "13:30"));
        assertThat(isOpened("2026-10-18T18:00", DiningType.DINNER, false)).isFalse();
    }

    @Test
    void 운영시간이_없는_요일과_식사는_예외_없이_미운영으로_판정한다() {
        assertThat(isOpened("2026-10-18T08:30", DiningType.BREAKFAST, false)).isFalse();
        assertThat(isOpened("2026-10-13T12:00", DiningType.LUNCH, false)).isFalse();
    }

    @Test
    void 시간이_null이면_미운영으로_판정한다() {
        replaceHours(hour(DayType.WEEKDAYS, "점심", null, null));
        assertThat(isOpened("2026-10-13T12:00", DiningType.LUNCH, false)).isFalse();
    }

    private boolean isOpened(String now, DiningType type, boolean isMinus) {
        return coopShopService.getIsOpened(LocalDateTime.parse(now), CoopShopType.CAFETERIA, type, isMinus);
    }

    private CoopOpen hour(DayType day, String type, String openTime, String closeTime) {
        return CoopOpen.builder().dayOfWeek(day).type(type).openTime(openTime).closeTime(closeTime).build();
    }

    private void replaceHours(CoopOpen... hours) {
        CoopShop shop = coopShopRepository.getByCoopNameIdAndCoopSemester(
            1, coopSemesterRepository.getByIsApplied(true));
        shop.getCoopOpens().clear();
        for (CoopOpen hour : hours) {
            hour.confirmCoopShop(shop);
        }
        entityManager.flush();
    }
}
