package in.koreatech.koin.admin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import in.koreatech.koin.fixture.BannerCategoryFixture;
import in.koreatech.koin.fixture.BannerFixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class AdminBannerApiTest extends AcceptanceTest {

    @Autowired
    private BannerFixture bannerFixture;

    @Autowired
    private BannerCategoryFixture bannerCategoryFixture;

    @Autowired
    private UserFixture userFixture;

    private Admin 어드민;
    private String 어드민_토큰;
    private String 생성일;
    private Banner 메인_배너_1;
    private Banner 메인_배너_2;
    private BannerCategory 배너_카테고리_메인_모달;

    @BeforeAll
    void setUp() {
        clear();
        어드민 = userFixture.코인_운영자();
        어드민_토큰 = userFixture.getToken(어드민.getUser());
        생성일 = "24.01.15";
        배너_카테고리_메인_모달 = bannerCategoryFixture.메인_모달();
        메인_배너_1 = bannerFixture.메인_배너_1(배너_카테고리_메인_모달);
        메인_배너_2 = bannerFixture.메인_배너_2(배너_카테고리_메인_모달);
    }

    @Test
    void 모든_메인_모달_배너를_페이지네이션으로_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/banners")
                    .header("Authorization", "Bearer " + 어드민_토큰)
                    .param("page", "1")
                    .param("banner_category_name", "메인 모달")
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                        "total_count": 2,
                        "current_count": 2,
                        "total_page": 1,
                        "current_page": 1,
                        "banners": [
                            {
                                "id": 1,
                                "banner_category": "메인 모달",
                                "priority": 1,
                                "title": "천원의 아침식사",
                                "image_url": "https://example.com/1000won.jpg",
                                "web_redirect_link": "https://example.com/1000won",
                                "android_redirect_link": "https://example.com/1000won",
                                "ios_redirect_link": "https://example.com/1000won",
                                "is_active": true,
                                "create_at": "%s"
                            },
                            {
                                "id": 2,
                                "banner_category": "메인 모달",
                                "priority": 2,
                                "title": "코인 이벤트",
                                "image_url": "https://example.com/koin-event.jpg",
                                "web_redirect_link": "https://example.com/koin-event",
                                "android_redirect_link": "https://example.com/koin-event",
                                "ios_redirect_link": "https://example.com/koin-event",
                                "is_active": true,
                                "create_at": "%s"
                            }
                        ]
                    }
                """, 생성일, 생성일)));
    }

    @Test
    void 단일_메인_모달_배너를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/banner/1")
                    .header("Authorization", "Bearer " + 어드민_토큰)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                        "id": 1,
                        "banner_category": "메인 모달",
                        "priority": 1,
                        "title": "천원의 아침식사",
                        "image_url": "https://example.com/1000won.jpg",
                        "web_redirect_link": "https://example.com/1000won",
                        "android_redirect_link": "https://example.com/1000won",
                        "ios_redirect_link": "https://example.com/1000won",
                        "is_active": true,
                        "create_at": "%s"
                    }
                """, 생성일)));
    }

    @Test
    void 메인_모달_배너를_생성한다() throws Exception {
        mockMvc.perform(
                post("/admin/banners")
                    .header("Authorization", "Bearer " + 어드민_토큰)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "banner_category_id": 1,
                                "title": "졸업학점 계산기",
                                "image_url": "https://example.com/1000won.jpg",
                                "web_redirect_link": "https://example.com/1000won",
                                "android_redirect_link": "https://example.com/1000won",
                                "ios_redirect_link": "https://example.com/1000won"
                            }
                        """)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 메인_모달_배너를_삭제한다() throws Exception {
        mockMvc.perform(
                delete("/admin/banner/1")
                    .header("Authorization", "Bearer " + 어드민_토큰)
            )
            .andExpect(status().isNoContent());
    }
}
