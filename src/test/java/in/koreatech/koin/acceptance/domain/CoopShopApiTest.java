package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.acceptance.fixture.CoopShopFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoopShopApiTest extends AcceptanceTest {

    @Autowired
    private CoopShopService coopShopService;

    @Autowired
    private CoopShopFixture coopShopFixture;

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
}
