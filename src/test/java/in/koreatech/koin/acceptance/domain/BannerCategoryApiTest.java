package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import in.koreatech.koin.acceptance.fixture.BannerCategoryAcceptanceFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BannerCategoryApiTest extends AcceptanceTest {

    @Autowired
    private BannerCategoryAcceptanceFixture bannerCategoryFixture;

    private BannerCategory 배너_카테고리_메인_모달;

    @BeforeAll
    void setup() {
        clear();
        배너_카테고리_메인_모달 = bannerCategoryFixture.메인_모달();
    }

    @Test
    void 배너_카테고리를_모두_조회한다() throws Exception {
        mockMvc.perform(
                get("/banner-categories")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                      "banner_categories": [
                        {
                          "id": 1,
                          "name": "메인 모달",
                        }
                      ]
                    }
                """));
    }
}
