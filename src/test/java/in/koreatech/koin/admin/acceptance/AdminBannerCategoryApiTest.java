package in.koreatech.koin.admin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import in.koreatech.koin.fixture.BannerCategoryFixture;
import in.koreatech.koin.fixture.UserFixture;

@Transactional
@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminBannerCategoryApiTest extends AcceptanceTest {

    @Autowired
    private BannerCategoryFixture bannerCategoryFixture;

    @Autowired
    private UserFixture userFixture;

    private Admin 어드민;
    private String 어드민_토큰;
    private BannerCategory 배너_카테고리_메인_모달;

    @BeforeAll
    void setUp() {
        clear();
        어드민 = userFixture.코인_운영자();
        어드민_토큰 = userFixture.getToken(어드민.getUser());
        배너_카테고리_메인_모달 = bannerCategoryFixture.메인_모달();
    }

    @Test
    void 모든_배너_카테고리를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/banner-categories")
                    .header("Authorization", "Bearer " + 어드민_토큰)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                      "banner_categories": [
                        {
                          "id": 1,
                          "name": "메인 모달",
                          "description": "140*112 앱/웹 랜딩시 뜨는 모달입니다."
                        }
                      ]
                    }
                """));
    }
}
