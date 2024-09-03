package in.koreatech.koin.admin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.CoopShopFixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AdminCoopShopTest extends AcceptanceTest {

    @Autowired
    private CoopShopRepository coopShopRepository;

    @Autowired
    private CoopShopFixture coopShopFixture;

    @Autowired
    private UserFixture userFixture;

    private CoopShop 학생식당;
    private CoopShop 세탁소;
    private User admin;
    private String token_admin;

    @BeforeAll
    void setUp() {
        학생식당 = coopShopFixture.학생식당();
        세탁소 = coopShopFixture.세탁소();
        admin = userFixture.코인_운영자();
        token_admin = userFixture.getToken(admin);
    }

    @Test
    void 생협의_모든_상점을_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/coopshop")
                    .header("Authorization", "Bearer " + token_admin)
                    .param("page", "1")
                    .param("is_deleted", "false")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                         "totalCount": 2,
                         "currentCount": 2,
                         "totalPage": 1,
                         "currentPage": 1,
                         "coopShops": [
                             {
                                 "id": 1,
                                 "name": "학생식당",
                                 "semester": "하계방학",
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
                                 "semester": "학기",
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
                     }
                """));
    }

    @Test
    void 생협의_특정_상점을_조회한다() throws Exception {
        mockMvc.perform(
                get("/coopshop/2")
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                        "id": 2,
                        "name": "세탁소",
                        "semester": "학기",
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
                """));
    }
}
