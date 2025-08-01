package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.BannerAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.BannerCategoryAcceptanceFixture;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;

public class BannerApiTest extends AcceptanceTest {

    @Autowired
    private BannerAcceptanceFixture bannerFixture;

    @Autowired
    private BannerCategoryAcceptanceFixture bannerCategoryFixture;

    private Banner 메인_배너_1;
    private Banner 메인_배너_2;
    private Banner 메인_배너_3;
    private BannerCategory 배너_카테고리_메인_모달;

    @BeforeAll
    void setUp() {
        clear();
        배너_카테고리_메인_모달 = bannerCategoryFixture.메인_모달();
        메인_배너_1 = bannerFixture.메인_배너_1(배너_카테고리_메인_모달);
        메인_배너_2 = bannerFixture.메인_배너_2(배너_카테고리_메인_모달);
        메인_배너_3 = bannerFixture.메인_배너_3(배너_카테고리_메인_모달);
    }

    @Test
    void 특정_카테고리의_활성화된_배너들을_플랫폼에따라_조회한다() throws Exception {
        mockMvc.perform(
                get("/banners/{categoryId}", 배너_카테고리_메인_모달.getId())
                    .param("platform", "ANDROID")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                        "count": 2,
                        "banners": [
                            {
                                "id": 1,
                                "title": "천원의 아침식사",
                                "image_url": "https://example.com/1000won.jpg",
                                "redirect_link": "koin://1000won"
                            },
                            {
                                "id": 2,
                                "title": "코인 이벤트",
                                "image_url": "https://example.com/koin-event.jpg",
                                "redirect_link": "koin://koin-event"
                            }
                        ]
                    }
                """));
    }
}
