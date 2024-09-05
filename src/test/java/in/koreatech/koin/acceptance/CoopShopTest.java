package in.koreatech.koin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.fixture.CoopShopFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoopShopTest extends AcceptanceTest {

    @Autowired
    private CoopShopRepository coopShopRepository;

    @Autowired
    private CoopShopFixture coopShopFixture;

    private CoopShop 학생식당;
    private CoopShop 세탁소;

    @BeforeAll
    void setUp() {
        clear();
        학생식당 = coopShopFixture.학생식당();
        세탁소 = coopShopFixture.세탁소();
    }

    @Test
    @Transactional
    void 생협의_모든_상점을_조회한다() throws Exception {
        mockMvc.perform(
                get("/coopshop")
                    .contentType(MediaType.ALL.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                        {
                            "id": 1,
                            "name": "학생식당",
                            "semester" : "하계방학",
                            "opens": [
                                {
                                    "day_of_week": "평일",
                                    "type": "아침",
                                    "open_time": "08:00",
                                    "close_time": "09:00"
                                },
                                {
                                    "day_of_week": "평일",
                                    "type": "점심",
                                    "open_time": "11:30",
                                    "close_time": "13:30"
                                },
                                {
                                    "day_of_week": "평일",
                                    "type": "저녁",
                                    "open_time": "17:30",
                                    "close_time": "18:30"
                                },
                                {
                                    "day_of_week": "주말",
                                    "type": "점심",
                                    "open_time": "11:30",
                                    "close_time": "13:00"
                                }
                            ],
                            "phone": "041-000-0000",
                            "location": "학생회관 1층",
                            "remarks": "공휴일 휴무",
                            "updated_at" : "2024-01-15"
                        },
                        {
                            "id": 2,
                            "name": "세탁소",
                            "semester" : "학기",
                            "opens": [
                                {
                                    "day_of_week": "평일",
                                    "type": null,
                                    "open_time": "09:00",
                                    "close_time": "18:00"
                                },
                                {
                                    "day_of_week": "주말",
                                    "type": null,
                                    "open_time": "미운영",
                                    "close_time": "미운영"
                                }
                            ],
                            "phone": "041-000-0000",
                            "location": "학생회관 2층",
                            "remarks": "연중무휴",
                            "updated_at" : "2024-01-15"
                        }
                    ]
                """));
    }

    @Test
    @Transactional
    void 생협의_상점을_조회한다() throws Exception {
        mockMvc.perform(
                get("/coopshop/1")
                    .contentType(MediaType.ALL.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                        "id": 1,
                        "name": "학생식당",
                        "semester" : "하계방학",
                        "opens": [
                            {
                                "day_of_week": "평일",
                                "type": "아침",
                                "open_time": "08:00",
                                "close_time": "09:00"
                            },
                            {
                                "day_of_week": "평일",
                                "type": "점심",
                                "open_time": "11:30",
                                "close_time": "13:30"
                            },
                            {
                                "day_of_week": "평일",
                                "type": "저녁",
                                "open_time": "17:30",
                                "close_time": "18:30"
                            },
                            {
                                "day_of_week": "주말",
                                "type": "점심",
                                "open_time": "11:30",
                                "close_time": "13:00"
                            }
                        ],
                        "phone": "041-000-0000",
                        "location": "학생회관 1층",
                        "remarks": "공휴일 휴무",
                        "updated_at" : "2024-01-15"
                }
                """));
    }
}
