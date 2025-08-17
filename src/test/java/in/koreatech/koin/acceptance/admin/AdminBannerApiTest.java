package in.koreatech.koin.acceptance.admin;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.BannerAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.BannerCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.banner.repository.AdminBannerRepository;
import in.koreatech.koin.admin.operator.model.Admin;
import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;

public class AdminBannerApiTest extends AcceptanceTest {

    @Autowired
    private BannerAcceptanceFixture bannerFixture;

    @Autowired
    private BannerCategoryAcceptanceFixture bannerCategoryFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AdminBannerRepository adminBannerRepository;

    private Admin 어드민;
    private String 어드민_토큰;
    private String 생성일;
    private Banner 메인_배너_1;
    private Banner 메인_배너_2;
    private Banner 메인_배너_3;
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
        메인_배너_3 = bannerFixture.메인_배너_3(배너_카테고리_메인_모달);
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
                        "total_count": 3,
                        "current_count": 3,
                        "total_page": 1,
                        "current_page": 1,
                        "banners": [
                            {
                                "id": 1,
                                "banner_category_id": 1,
                                "banner_category": "메인 모달",
                                "priority": 1,
                                "title": "천원의 아침식사",
                                "image_url": "https://example.com/1000won.jpg",
                                "is_web_released": true,
                                "web_redirect_link": "https://example.com/1000won",
                                "android_redirect_link": "koin://1000won",
                                "android_minimum_version": "3.0.14",
                                "is_ios_released": true,
                                "ios_redirect_link": "https://example.com/1000won",
                                "ios_minimum_version": "3.0.14",
                                "is_active": true,
                                "created_at": "%s"
                            },
                            {
                                "id": 2,
                                "banner_category_id": 1,
                                "banner_category": "메인 모달",
                                "priority": 2,
                                "title": "코인 이벤트",
                                "image_url": "https://example.com/koin-event.jpg",
                                "is_web_released": true,
                                "web_redirect_link": "https://example.com/koin-event",
                                "android_redirect_link": "koin://koin-event",
                                "android_minimum_version": "3.0.14",
                                "is_ios_released": true,
                                "ios_redirect_link": "https://example.com/koin-event",
                                "ios_minimum_version": "3.0.14",
                                "is_active": true,
                                "created_at": "%s"
                            },
                            {
                                "id": 3,
                                "banner_category_id": 1,
                                "banner_category": "메인 모달",
                                "priority": null,
                                "title": "코인 이벤트 누누",
                                "image_url": "https://example.com/nunu-event.jpg",
                                "is_web_released": true,
                                "web_redirect_link": "https://example.com/nunu-event",
                                "android_redirect_link": "koin://nunu-event",
                                "android_minimum_version": "3.0.14",
                                "is_ios_released": true,
                                "ios_redirect_link": "https://example.com/nunu-event",
                                "ios_minimum_version": "3.0.14",
                                "is_active": false,
                                "created_at": "%s"
                            }
                        ]
                    }
                """, 생성일, 생성일, 생성일)));
    }

    @Test
    void 단일_메인_모달_배너를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/banners/1")
                    .header("Authorization", "Bearer " + 어드민_토큰)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                    {
                        "id": 1,
                        "banner_category_id": 1,
                        "banner_category": "메인 모달",
                        "priority": 1,
                        "title": "천원의 아침식사",
                        "image_url": "https://example.com/1000won.jpg",
                        "is_web_released": true,
                        "web_redirect_link": "https://example.com/1000won",
                        "android_redirect_link": "koin://1000won",
                        "android_minimum_version": "3.0.14",
                        "is_ios_released": true,
                        "ios_redirect_link": "https://example.com/1000won",
                        "ios_minimum_version": "3.0.14",
                        "is_active": true,
                        "created_at": "%s"
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
                                "is_web_released": true,
                                "web_redirect_link": "https://example.com/1000won",
                                "is_android_released": true,
                                "android_redirect_link": "koin://1000won",
                                "android_minimum_version": "3.0.14",
                                "is_ios_released": true,
                                "ios_redirect_link": "https://example.com/1000won",
                                "ios_minimum_version": "3.0.14"
                            }
                        """)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 모바일_리다이렉션_링크는_없고_모바일_최소버전이_있으면_예외를_발생한다() throws Exception {
        mockMvc.perform(
                post("/admin/banners")
                    .header("Authorization", "Bearer " + 어드민_토큰)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "banner_category_id": 1,
                                "title": "졸업학점 계산기",
                                "image_url": "https://example.com/1000won.jpg",
                                "is_web_released": true,
                                "web_redirect_link": "https://example.com/1000won",
                                "is_android_released": true,
                                "android_redirect_link": "koin://1000won",
                                "is_ios_released": true,
                                "ios_redirect_link": "https://example.com/1000won",
                                "ios_minimum_version": "3.0.14"
                            }
                        """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 메인_모달_배너를_삭제한다() throws Exception {
        mockMvc.perform(
                delete("/admin/banners/1")
                    .header("Authorization", "Bearer " + 어드민_토큰)
            )
            .andExpect(status().isNoContent());

        transactionTemplate.executeWithoutResult(status -> {
            Banner priorityUpdatedBanner = adminBannerRepository.getById(메인_배너_2.getId());
            assertSoftly(softly -> {
                softly.assertThat(priorityUpdatedBanner.getPriority()).isEqualTo(1);
            });
        });
    }

    @Test
    void 배너_우선순위를_올린다() throws Exception {
        mockMvc.perform(
                patch("/admin/banners/{id}/priority", 메인_배너_2.getId())
                    .header("Authorization", "Bearer " + 어드민_토큰)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                               "change_type": "UP"
                            }
                        """)
            )
            .andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            Banner updatedBanner = adminBannerRepository.getById(메인_배너_2.getId());
            assertSoftly(softly -> {
                softly.assertThat(updatedBanner.getPriority()).isEqualTo(1);
            });
        });
    }

    @Test
    void 배너_우선순위를_내린다() throws Exception {
        mockMvc.perform(
                patch("/admin/banners/{id}/priority", 메인_배너_1.getId())
                    .header("Authorization", "Bearer " + 어드민_토큰)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                               "change_type": "DOWN"
                            }
                        """)
            )
            .andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            Banner updatedBanner = adminBannerRepository.getById(메인_배너_1.getId());
            assertSoftly(softly -> {
                softly.assertThat(updatedBanner.getPriority()).isEqualTo(2);
            });
        });
    }

    @Test
    void 배너_활성화상태에서_비활성화한다() throws Exception {
        mockMvc.perform(
            patch("/admin/banners/{id}/active", 메인_배너_1.getId())
                .header("Authorization", "Bearer " + 어드민_토큰)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                           "is_active": "false"
                        }
                    """)
        ).andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            Banner updatedBanner = adminBannerRepository.getById(메인_배너_1.getId());
            assertSoftly(softly -> {
                softly.assertThat(updatedBanner.getIsActive()).isEqualTo(false);
                softly.assertThat(updatedBanner.getPriority()).isNull();
            });
        });
    }

    @Test
    void 배너_비활성화상태에서_활성화한다() throws Exception {
        mockMvc.perform(
            patch("/admin/banners/{id}/active", 메인_배너_3.getId())
                .header("Authorization", "Bearer " + 어드민_토큰)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                           "is_active": "true"
                        }
                    """)
        ).andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            Banner updatedBanner = adminBannerRepository.getById(메인_배너_3.getId());
            assertSoftly(softly -> {
                softly.assertThat(updatedBanner.getIsActive()).isEqualTo(true);
                softly.assertThat(updatedBanner.getPriority()).isEqualTo(3);
            });
        });
    }

    @Test
    void 배너_상세를_수정한다() throws Exception {
        mockMvc.perform(
            put("/admin/banners/{id}", 메인_배너_3.getId())
                .header("Authorization", "Bearer " + 어드민_토큰)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                           "title": "새제목",
                           "image_url": "https://example.com/new1000won.jpg",
                           "is_web_released": "true",
                           "web_redirect_link": "https://example.com/new1000won.jpg",
                           "is_android_released": "true",
                           "android_redirect_link": "koin://new1000won",
                           "android_minimum_version": "3.0.14",
                           "is_ios_released": "false",
                           "is_active": "true"
                        }
                    """)
        ).andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            Banner updatedBanner = adminBannerRepository.getById(메인_배너_3.getId());
            assertSoftly(softly -> {
                softly.assertThat(updatedBanner.getTitle()).isEqualTo("새제목");
                softly.assertThat(updatedBanner.getImageUrl())
                    .isEqualTo("https://example.com/new1000won.jpg");
                softly.assertThat(updatedBanner.getWebRedirectLink())
                    .isEqualTo("https://example.com/new1000won.jpg");
                softly.assertThat(updatedBanner.getAndroidRedirectLink())
                    .isEqualTo("koin://new1000won");
                softly.assertThat(updatedBanner.getAndroidMinimumVersion())
                    .isEqualTo("3.0.14");
                softly.assertThat(updatedBanner.getIosRedirectLink()).isNull();
                softly.assertThat(updatedBanner.getIsActive()).isEqualTo(true);
                softly.assertThat(updatedBanner.getPriority()).isEqualTo(3);
            });
        });
    }
}
