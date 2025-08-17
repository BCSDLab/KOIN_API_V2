package in.koreatech.koin.acceptance.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CoopShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.operator.model.Admin;
import in.koreatech.koin.domain.coopshop.repository.CoopShopRepository;

class AdminCoopShopApiTest extends AcceptanceTest {

    @Autowired
    private CoopShopRepository coopShopRepository;

    @Autowired
    private CoopShopAcceptanceFixture coopShopFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    private Admin admin;
    private String token_admin;

    @BeforeAll
    void setUp() {
        clear();
        coopShopFixture._23_2학기();
        coopShopFixture._23_겨울학기();
        admin = userFixture.코인_운영자();
        token_admin = userFixture.getToken(admin.getUser());
    }

    @Test
    void 모든_생협_운영_학기를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/coopshop")
                    .header("Authorization", "Bearer " + token_admin)
                    .param("page", "1")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                         "total_count": 2,
                         "current_count": 2,
                         "total_page": 1,
                         "current_page": 1,
                         "coop_shop_semesters": [
                             {
                                 "id": 1,
                                 "semester": "23-2학기",
                                 "from_date": "2023-09-02",
                                 "to_date": "2023-12-20",
                                 "updated_at" : "2024-01-15 12:00:00"
                             },
                             {
                                 "id": 2,
                                 "semester": "23-겨울학기",
                                 "from_date": "2023-12-21",
                                 "to_date": "2024-02-28",
                                 "updated_at" : "2024-01-15 12:00:00"
                             }
                         ]
                     }
                """));
    }

    @Test
    void 특정_학기의_생협_정보를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/coopshop/1")
                    .header("Authorization", "Bearer " + token_admin)
                    .param("page", "1")
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
}
