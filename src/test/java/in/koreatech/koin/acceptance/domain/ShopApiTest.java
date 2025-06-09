package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.DISMISSED;
import static in.koreatech.koin.domain.shop.model.review.ReportStatus.UNHANDLED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.benefit.model.BenefitCategory;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.acceptance.fixture.BenefitCategoryFixture;
import in.koreatech.koin.acceptance.fixture.BenefitCategoryMapFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentFixture;
import in.koreatech.koin.acceptance.fixture.EventArticleFixture;
import in.koreatech.koin.acceptance.fixture.MenuCategoryFixture;
import in.koreatech.koin.acceptance.fixture.MenuFixture;
import in.koreatech.koin.acceptance.fixture.ShopCategoryFixture;
import in.koreatech.koin.acceptance.fixture.ShopFixture;
import in.koreatech.koin.acceptance.fixture.ShopNotificationMessageFixture;
import in.koreatech.koin.acceptance.fixture.ShopParentCategoryFixture;
import in.koreatech.koin.acceptance.fixture.ShopReviewFixture;
import in.koreatech.koin.acceptance.fixture.ShopReviewReportFixture;
import in.koreatech.koin.acceptance.fixture.UserFixture;

@Transactional
@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShopApiTest extends AcceptanceTest {

    @Autowired
    private BenefitCategoryFixture benefitCategoryFixture;

    @Autowired
    private BenefitCategoryMapFixture benefitCategoryMapFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ShopFixture shopFixture;

    @Autowired
    private ShopReviewFixture shopReviewFixture;

    @Autowired
    private ShopReviewReportFixture shopReviewReportFixture;

    @Autowired
    private MenuFixture menuFixture;

    @Autowired
    private MenuCategoryFixture menuCategoryFixture;

    @Autowired
    private EventArticleFixture eventArticleFixture;

    @Autowired
    private ShopCategoryFixture shopCategoryFixture;

    @Autowired
    private ShopParentCategoryFixture shopParentCategoryFixture;

    @Autowired
    private ShopNotificationMessageFixture shopNotificationMessageFixture;

    @Autowired
    private DepartmentFixture departmentFixture;

    private Shop 마슬랜;
    private Owner owner;

    private Student 익명_학생;
    private String token_익명;
    private Department department;

    private ShopParentCategory shopParentCategory_가게;
    private ShopNotificationMessage notificationMessage_가게;

    private ShopCategory shopCategory_치킨;

    @BeforeAll
    void setUp() {
        clear();
        owner = userFixture.준영_사장님();
        익명_학생 = userFixture.익명_학생(department);
        token_익명 = userFixture.getToken(익명_학생.getUser());
        notificationMessage_가게 = shopNotificationMessageFixture.알림메시지_가게();
        shopParentCategory_가게 = shopParentCategoryFixture.상위_카테고리_가게(notificationMessage_가게);
        shopCategory_치킨 = shopCategoryFixture.카테고리_치킨(shopParentCategory_가게);
        마슬랜 = shopFixture.마슬랜(owner, shopCategory_치킨);
    }

    @Test
    void 옵션이_하나_있는_상점의_메뉴를_조회한다() throws Exception {
        Menu menu = menuFixture.짜장면_단일메뉴(마슬랜, menuCategoryFixture.메인메뉴(마슬랜));
        mockMvc.perform(
                        get("/shops/{shopId}/menus/{menuId}", menu.getShop().getId(), menu.getId())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "id": 1,
                            "shop_id": 1,
                            "name": "짜장면",
                            "is_hidden": false,
                            "is_single": true,
                            "single_price": 7000,
                            "option_prices": null,
                            "description": "맛있는 짜장면",
                            "category_ids": [
                                1
                            ],
                            "image_urls": [
                                "https://test.com/짜장면.jpg",
                                "https://test.com/짜장면22.jpg"
                            ]
                        }
                        """)
                );
    }

    @Test
    void 옵션이_여러_개_있는_상점의_메뉴를_조회한다() throws Exception {
        Menu menu = menuFixture.짜장면_옵션메뉴(마슬랜, menuCategoryFixture.메인메뉴(마슬랜));

        mockMvc.perform(
                        get("/shops/{shopId}/menus/{menuId}", menu.getShop().getId(), menu.getId())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "id": 1,
                            "shop_id": 1,
                            "name": "짜장면",
                            "is_hidden": false,
                            "is_single": false,
                            "single_price": null,
                            "option_prices": [
                                {
                                    "option": "곱빼기",
                                    "price": 7500
                                },
                                {
                                    "option": "일반",
                                    "price": 7000
                                }
                            ],
                            "description": "맛있는 짜장면",
                            "category_ids": [
                                1
                            ],
                            "image_urls": [
                                "https://test.com/짜장면.jpg",
                                "https://test.com/짜장면22.jpg"
                            ]
                        }
                        """));
    }

    @Test
    void 상점의_메뉴_카테고리들을_조회한다() throws Exception {
        menuCategoryFixture.사이드메뉴(마슬랜);
        menuCategoryFixture.세트메뉴(마슬랜);
        Menu menu = menuFixture.짜장면_단일메뉴(마슬랜, menuCategoryFixture.추천메뉴(마슬랜));

        mockMvc.perform(
                        get("/shops/{shopId}/menus/categories", menu.getShop().getId())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                                "count": 3,
                                "menu_categories": [
                                    {
                                        "id": 3,
                                        "name": "추천 메뉴"
                                    },
                                    {
                                        "id": 2,
                                        "name": "세트 메뉴"
                                    },
                                    {
                                        "id": 1,
                                        "name": "사이드 메뉴"
                                    }
                                ]
                            }
                        """));
    }

    @Test
    void 특정_상점_조회() throws Exception {
        menuCategoryFixture.사이드메뉴(마슬랜);
        menuCategoryFixture.세트메뉴(마슬랜);

        mockMvc.perform(
                        get("/shops/{shopId}", 마슬랜.getId())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                            {
                                 "address": "천안시 동남구 병천면 1600",
                                 "delivery": true,
                                 "delivery_price": 3000,
                                 "description": "마슬랜 치킨입니다.",
                                 "id": 1,
                                 "image_urls": [
                                     "https://test-image.com/마슬랜.png",
                                     "https://test-image.com/마슬랜2.png"
                                 ],
                                 "menu_categories": [
                                     {
                                         "id": 2,
                                         "name": "세트 메뉴"
                                     },
                                     {
                                         "id": 1,
                                         "name": "사이드 메뉴"
                                     }
                                 ],
                                 "name": "마슬랜 치킨",
                                 "open": [
                                     {
                                         "day_of_week": "MONDAY",
                                         "closed": false,
                                         "open_time": "00:00",
                                         "close_time": "21:00"
                                     },
                                     {
                                         "day_of_week": "FRIDAY",
                                         "closed": false,
                                         "open_time": "00:00",
                                         "close_time": "00:00"
                                     }
                                 ],
                                 "pay_bank": true,
                                 "pay_card": true,
                                 "phone": "010-7574-1212",
                                 "shop_categories": [
                                   \s
                                 ],
                                 "updated_at": "2024-01-15",
                                 "is_event": false,
                                 "bank": "국민",
                                 "account_number": "01022595923"
                             }
                        """));
    }

    @Test
    void 특정_상점_모든_메뉴_조회() throws Exception {
        menuFixture.짜장면_단일메뉴(마슬랜, menuCategoryFixture.추천메뉴(마슬랜));
        menuFixture.짜장면_옵션메뉴(마슬랜, menuCategoryFixture.세트메뉴(마슬랜));
        mockMvc.perform(
                        get("/shops/{id}/menus", 마슬랜.getId())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                                "count": 2,
                                "menu_categories": [
                                    {
                                        "id": 1,
                                        "name": "추천 메뉴",
                                        "menus": [
                                            {
                                                "id": 1,
                                                "name": "짜장면",
                                                "is_hidden": false,
                                                "is_single": true,
                                                "single_price": 7000,
                                                "option_prices": null,
                                                "description": "맛있는 짜장면",
                                                "image_urls": [
                                                    "https://test.com/짜장면.jpg",
                                                    "https://test.com/짜장면22.jpg"
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        "id": 2,
                                        "name": "세트 메뉴",
                                        "menus": [
                                            {
                                                "id": 2,
                                                "name": "짜장면",
                                                "is_hidden": false,
                                                "is_single": false,
                                                "single_price": null,
                                                "option_prices": [
                                                    {
                                                        "option": "곱빼기",
                                                        "price": 7500
                                                    },
                                                    {
                                                        "option": "일반",
                                                        "price": 7000
                                                    }
                                                ],
                                                "description": "맛있는 짜장면",
                                                "image_urls": [
                                                    "https://test.com/짜장면.jpg",
                                                    "https://test.com/짜장면22.jpg"
                                                ]
                                            }
                                        ]
                                    }
                                ],
                                "updated_at": "2024-01-15"
                            }
                        """));
    }

    @Test
    void 모든_상점_조회() throws Exception {
        shopFixture.영업중이_아닌_신전_떡볶이(owner);

        // 2024-01-15 12:00 월요일 기준
        boolean 마슬랜_영업여부 = true;
        boolean 신전_떡볶이_영업여부 = false;

        mockMvc.perform(
                        get("/shops")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                            {
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "is_event": false,
                                    "is_open": %s
                                },{
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 2,
                                    "name": "신전 떡볶이",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "12:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "is_event": false,
                                    "is_open": %s
                                }
                            ]
                        }
                        """, 마슬랜_영업여부, 신전_떡볶이_영업여부)));
    }

    @Test
    void 상점의_정렬된_모든_카테고리_조회() throws Exception {
        shopCategoryFixture.카테고리_일반음식(shopParentCategory_가게); // 카테고리_치킨이 먼저 생성됨

        mockMvc.perform(
                        get("/shops/categories")
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "total_count": 2,
                            "shop_categories": [
                                {
                                    "id": 2,
                                    "name": "일반음식점",
                                    "image_url": "https://test-image.com/normal.jpg"
                                },
                                {
                                    "id": 1,
                                    "name": "치킨",
                                    "image_url": "https://test-image.com/ckicken.jpg"
                                }
                            ]
                        }
                        """));
    }

    @Test
    void 특정_상점의_이벤트들을_조회한다() throws Exception {
        eventArticleFixture.할인_이벤트(마슬랜, LocalDate.now(clock).minusDays(3), LocalDate.now(clock).plusDays(3));
        eventArticleFixture.참여_이벤트(마슬랜, LocalDate.now(clock).minusDays(3), LocalDate.now(clock).plusDays(3));

        mockMvc.perform(
                        get("/shops/{shopId}/events", 마슬랜.getId())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "events": [
                                {
                                    "shop_id": 1,
                                    "shop_name": "마슬랜 치킨",
                                    "event_id": 1,
                                    "title": "할인 이벤트",
                                    "content": "사장님이 미쳤어요!",
                                    "thumbnail_images": [
                                        "https://eventimage.com/할인_이벤트.jpg",
                                        "https://eventimage.com/할인_이벤트.jpg"
                                    ],
                                    "start_date": "2024-01-12",
                                    "end_date": "2024-01-18"
                                },
                                {
                                    "shop_id": 1,
                                    "shop_name": "마슬랜 치킨",
                                    "event_id": 2,
                                    "title": "참여 이벤트",
                                    "content": "사장님과 참여해요!!!",
                                    "thumbnail_images": [
                                        "https://eventimage.com/참여_이벤트.jpg",
                                        "https://eventimage.com/참여_이벤트.jpg"
                                    ],
                                    "start_date": "2024-01-12",
                                    "end_date": "2024-01-18"
                                }
                            ]
                        }
                        """));
    }

    @Test
    void 이벤트_진행중인_상점의_정보를_조회한다() throws Exception {
        eventArticleFixture.할인_이벤트(마슬랜, LocalDate.now(clock).minusDays(3), LocalDate.now(clock).plusDays(3));
        eventArticleFixture.참여_이벤트(마슬랜, LocalDate.now(clock).minusDays(3), LocalDate.now(clock).plusDays(3));

        mockMvc.perform(
                        get("/shops/{shopId}", 마슬랜.getId())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "is_event": true
                        }
                        """));
    }

    @Test
    void 이벤트_진행중이지_않은_상점의_정보를_조회한다() throws Exception {
        eventArticleFixture.할인_이벤트(마슬랜, LocalDate.now(clock).plusDays(3), LocalDate.now(clock).plusDays(5));
        eventArticleFixture.참여_이벤트(마슬랜, LocalDate.now(clock).minusDays(5), LocalDate.now(clock).minusDays(3));

        mockMvc.perform(
                        get("/shops/{shopId}", 마슬랜.getId())
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "is_event": false
                        }
                        """));
    }

    @Test
    void 이벤트_베너_조회() throws Exception {
        eventArticleFixture.참여_이벤트(마슬랜, LocalDate.now(clock), LocalDate.now(clock).plusDays(10));
        eventArticleFixture.할인_이벤트(마슬랜, LocalDate.now(clock).minusDays(10), LocalDate.now(clock).minusDays(1));

        mockMvc.perform(
                        get("/shops/events")
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "events": [
                                {
                                    "shop_id": 1,
                                    "shop_name": "마슬랜 치킨",
                                    "event_id": 1,
                                    "title": "참여 이벤트",
                                    "content": "사장님과 참여해요!!!",
                                    "thumbnail_images": [
                                        "https://test-image.com/chicken-event.jpg"
                                    ],
                                    "start_date": "2024-01-15",
                                    "end_date": "2024-01-25"
                                }
                            ]
                        }
                        """));
    }

    @Test
    void 리뷰_평점순으로_정렬하여_모든_상점을_조회한다() throws Exception {
        Shop 영업중인_티바 = shopFixture.영업중인_티바(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중인_티바);
        boolean 마슬랜_영업여부 = true;
        boolean 티바_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("sorter", "RATING")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 2,
                                    "name": "티바",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                },{
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 0.0,
                                    "review_count": 0
                                }
                            ]
                        }
                        """, 티바_영업여부, 마슬랜_영업여부)));
    }

    @Test
    void 리뷰_개수순으로_정렬하여_모든_상점을_조회한다() throws Exception {
        Shop 영업중인_티바 = shopFixture.영업중인_티바(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중인_티바);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 마슬랜_영업여부 = true;
        boolean 티바_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("sorter", "COUNT")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 2
                                },{
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 2,
                                    "name": "티바",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 티바_영업여부, 마슬랜_영업여부)));
    }

    @Test
    void 리뷰_개수가_많아도_영업중이_아니라면_정렬_우선순위가_낮은_상태로_모든_상점을_조회한다() throws Exception {
        Shop 영업중이_아닌_신전떡볶이 = shopFixture.영업중이_아닌_신전_떡볶이(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중이_아닌_신전떡볶이);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중이_아닌_신전떡볶이);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 신전떡볶이_영업여부 = false;
        boolean 마슬랜_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("sorter", "COUNT")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                },{
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 2,
                                    "name": "신전 떡볶이",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "12:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 2
                                }
                            ]
                        }
                        """, 마슬랜_영업여부, 신전떡볶이_영업여부)));
    }

    @Test
    void 운영중인_상점만_필터하여_모든_상점을_조회한다() throws Exception {
        Shop 영업중이_아닌_신전떡볶이 = shopFixture.영업중이_아닌_신전_떡볶이(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중이_아닌_신전떡볶이);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중이_아닌_신전떡볶이);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);

        // 2024-01-15 12:00 월요일 기준
        boolean 마슬랜_영업여부 = true;

        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("filter", "OPEN")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 1,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 마슬랜_영업여부)));
    }

    @Test
    void 배달_가능한_상점만_필터하여_모든_상점을_조회한다() throws Exception {
        Shop 배달_안되는_신전_떡볶이 = shopFixture.배달_안되는_신전_떡볶이(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 배달_안되는_신전_떡볶이);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 마슬랜_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("filter", "DELIVERY")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 1,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 마슬랜_영업여부)));
    }

    @Test
    void 배달_가능하고_영업중인_상점만_필터하여_모든_상점을_조회한다() throws Exception {
        Shop 배달_안되는_신전_떡볶이 = shopFixture.배달_안되는_신전_떡볶이(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 배달_안되는_신전_떡볶이);
        shopReviewFixture.리뷰_4점(익명_학생, 배달_안되는_신전_떡볶이);

        shopFixture.영업중이_아닌_신전_떡볶이(owner);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 마슬랜_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("filter", "DELIVERY")
                                .queryParam("filter", "OPEN")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 1,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 마슬랜_영업여부)));
    }

    @Test
    void 영업중인_상점만_필터하여_리뷰_개수_순으로_모든_상점을_조회한다() throws Exception {
        Shop 배달_안되는_신전_떡볶이 = shopFixture.배달_안되는_신전_떡볶이(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 배달_안되는_신전_떡볶이);
        shopReviewFixture.리뷰_4점(익명_학생, 배달_안되는_신전_떡볶이);

        shopFixture.영업중이_아닌_신전_떡볶이(owner);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 신전_떡볶이_영업여부 = true;
        boolean 마슬랜_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("filter", "OPEN")
                                .queryParam("sorter", "COUNT")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": false,
                                    "id": 2,
                                    "name": "신전 떡볶이",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 2
                                },{
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 신전_떡볶이_영업여부, 마슬랜_영업여부)));
    }

    @Test
    void 신고된_리뷰의_내용도_반영해서_모든_상점을_조회한다() throws Exception {
        Shop 배달_안되는_신전_떡볶이 = shopFixture.배달_안되는_신전_떡볶이(owner);
        ShopReview 리뷰_4점 = shopReviewFixture.리뷰_4점(익명_학생, 배달_안되는_신전_떡볶이);
        shopReviewReportFixture.리뷰_신고(익명_학생, 리뷰_4점, UNHANDLED);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 신전_떡볶이_영업여부 = true;
        boolean 마슬랜_영업여부 = true;

        mockMvc.perform(
                        get("/v2/shops")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                },{
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": false,
                                    "id": 2,
                                    "name": "신전 떡볶이",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 마슬랜_영업여부, 신전_떡볶이_영업여부)));
    }

    @Test
    void 신고_반려된_리뷰는_반영된_상태로_모든_상점을_조회한다() throws Exception {
        Shop 배달_안되는_신전_떡볶이 = shopFixture.배달_안되는_신전_떡볶이(owner);
        ShopReview 리뷰_4점 = shopReviewFixture.리뷰_4점(익명_학생, 배달_안되는_신전_떡볶이);
        shopReviewReportFixture.리뷰_신고(익명_학생, 리뷰_4점, DISMISSED);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 신전_떡볶이_영업여부 = true;
        boolean 마슬랜_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                },{
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": false,
                                    "id": 2,
                                    "name": "신전 떡볶이",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 마슬랜_영업여부, 신전_떡볶이_영업여부)));
    }

    @Test
    void _24시간_운영중인_상점은_is_open은_true이다() throws Exception {
        Shop 영업중인_티바 = shopFixture._24시간_영업중인_티바(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중인_티바);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 마슬랜_영업여부 = true;
        boolean 티바_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("sorter", "COUNT")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "21:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 2
                                },{
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 2,
                                    "name": "티바",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 티바_영업여부, 마슬랜_영업여부)));
    }

    @Test
    void 검색어를_입력해서_상점을_조회한다() throws Exception {
        Shop 배달_안되는_신전_떡볶이 = shopFixture.배달_안되는_신전_떡볶이(owner);
        ShopReview 리뷰_4점 = shopReviewFixture.리뷰_4점(익명_학생, 배달_안되는_신전_떡볶이);
        shopReviewReportFixture.리뷰_신고(익명_학생, 리뷰_4점, DISMISSED);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 신전_떡볶이_영업여부 = true;
        boolean 마슬랜_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("query", "떡")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 1,
                            "shops": [
                                {
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": false,
                                    "id": 2,
                                    "name": "신전 떡볶이",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 마슬랜_영업여부, 신전_떡볶이_영업여부)));
    }

    @Test
    void 리뷰_평점기준_오름차순_정렬하여_모든_상점을_조회한다() throws Exception {
        Shop 영업중인_티바 = shopFixture.영업중인_티바(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중인_티바);
        boolean 마슬랜_영업여부 = true;
        boolean 티바_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("sorter", "RATING_ASC")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                    "category_ids": [
                                           \s
                                        ],
                                        "delivery": true,
                                        "id": 1,
                                        "name": "마슬랜 치킨",
                                        "pay_bank": true,
                                        "pay_card": true,
                                        "phone": "010-7574-1212",
                                        "open": [
                                            {
                                                "day_of_week": "MONDAY",
                                                "closed": false,
                                                "open_time": "00:00",
                                                "close_time": "21:00"
                                            },
                                            {
                                                "day_of_week": "FRIDAY",
                                                "closed": false,
                                                "open_time": "00:00",
                                                "close_time": "00:00"
                                            }
                                        ],
                                        "is_event": false,
                                        "is_open": %s,
                                        "average_rate": 0.0,
                                        "review_count": 0
                                },
                                {
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 2,
                                    "name": "티바",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "open": [
                                        {
                                            "day_of_week": "MONDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "TUESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "WEDNESDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "THURSDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "FRIDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SATURDAY",
                                            "closed": false,
                                            "open_time": "11:30",
                                            "close_time": "21:30"
                                        },
                                        {
                                            "day_of_week": "SUNDAY",
                                            "closed": false,
                                            "open_time": "00:00",
                                            "close_time": "00:00"
                                        }
                                    ],
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 티바_영업여부, 마슬랜_영업여부)));
    }

    @Test
    void 리뷰_개수기준_오름차순_정렬하여_모든_상점을_조회한다() throws Exception {
        Shop 영업중인_티바 = shopFixture.영업중인_티바(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중인_티바);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 마슬랜_영업여부 = true;
        boolean 티바_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("sorter", "COUNT_ASC")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                            {
                                "category_ids": [
                                   \s
                                ],
                                "delivery": true,
                                "id": 2,
                                "name": "티바",
                                "pay_bank": true,
                                "pay_card": true,
                                "phone": "010-7788-9900",
                                "open": [
                                    {
                                        "day_of_week": "MONDAY",
                                        "closed": false,
                                        "open_time": "11:30",
                                        "close_time": "21:30"
                                    },
                                    {
                                        "day_of_week": "TUESDAY",
                                        "closed": false,
                                        "open_time": "11:30",
                                        "close_time": "21:30"
                                    },
                                    {
                                        "day_of_week": "WEDNESDAY",
                                        "closed": false,
                                        "open_time": "11:30",
                                        "close_time": "21:30"
                                    },
                                    {
                                        "day_of_week": "THURSDAY",
                                        "closed": false,
                                        "open_time": "11:30",
                                        "close_time": "21:30"
                                    },
                                    {
                                        "day_of_week": "FRIDAY",
                                        "closed": false,
                                        "open_time": "11:30",
                                        "close_time": "21:30"
                                    },
                                    {
                                        "day_of_week": "SATURDAY",
                                        "closed": false,
                                        "open_time": "11:30",
                                        "close_time": "21:30"
                                    },
                                    {
                                        "day_of_week": "SUNDAY",
                                        "closed": false,
                                        "open_time": "00:00",
                                        "close_time": "00:00"
                                    }
                                ],
                                "is_event": false,
                                "is_open": %s,
                                "average_rate": 4.0,
                                "review_count": 1
                            },{
                                "category_ids": [
                                       \s
                                ],
                                "delivery": true,
                                "id": 1,
                                "name": "마슬랜 치킨",
                                "pay_bank": true,
                                "pay_card": true,
                                "phone": "010-7574-1212",
                                "open": [
                                    {
                                        "day_of_week": "MONDAY",
                                        "closed": false,
                                        "open_time": "00:00",
                                        "close_time": "21:00"
                                    },
                                    {
                                        "day_of_week": "FRIDAY",
                                        "closed": false,
                                        "open_time": "00:00",
                                        "close_time": "00:00"
                                    }
                                ],
                                "is_event": false,
                                "is_open": %s,
                                "average_rate": 4.0,
                                "review_count": 2
                                }
                            ]
                        }
                        """, 티바_영업여부, 마슬랜_영업여부)));
    }

    @Test
    void 리뷰_평점기준_내림차순_정렬하여_모든_상점을_조회한다() throws Exception {
        Shop 영업중인_티바 = shopFixture.영업중인_티바(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중인_티바);
        boolean 마슬랜_영업여부 = true;
        boolean 티바_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("sorter", "RATING_DESC")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 2,
                                    "name": "티바",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                },{
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 0.0,
                                    "review_count": 0
                                }
                            ]
                        }
                        """, 티바_영업여부, 마슬랜_영업여부)));
    }

    @Test
    void 리뷰_개수기준_내림차순_정렬하여_모든_상점을_조회한다() throws Exception {
        Shop 영업중인_티바 = shopFixture.영업중인_티바(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중인_티바);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 마슬랜_영업여부 = true;
        boolean 티바_영업여부 = true;
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("sorter", "COUNT_DESC")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 2
                                },{
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 2,
                                    "name": "티바",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1
                                }
                            ]
                        }
                        """, 티바_영업여부, 마슬랜_영업여부)));
    }

    @Test
    void 전화하기_발생시_정보가_알림큐에_저장된다() throws Exception {
        mockMvc.perform(
                        post("/shops/{shopId}/call-notification", 마슬랜.getId())
                                .header("Authorization", "Bearer " + token_익명)
                )
                .andExpect(status().isOk());
    }

    @Test
    void 리뷰를_조회하면_혜택_정보가_조회된다() throws Exception {
        Shop 영업중인_티바 = shopFixture.영업중인_티바(owner);
        shopReviewFixture.리뷰_4점(익명_학생, 영업중인_티바);

        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        shopReviewFixture.리뷰_4점(익명_학생, 마슬랜);
        // 2024-01-15 12:00 월요일 기준
        boolean 마슬랜_영업여부 = true;
        boolean 티바_영업여부 = true;

        BenefitCategory 최소주문금액_무료 = benefitCategoryFixture.최소주문금액_무료();
        BenefitCategory 서비스_증정 = benefitCategoryFixture.서비스_증정();
        benefitCategoryMapFixture.설명이_포함된_혜택_추가(영업중인_티바, 최소주문금액_무료, "무료");
        benefitCategoryMapFixture.설명이_포함된_혜택_추가(영업중인_티바, 서비스_증정, "콜라");
        mockMvc.perform(
                        get("/v2/shops")
                                .queryParam("sorter", "COUNT_DESC")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "count": 2,
                            "shops": [
                                {
                                "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 1,
                                    "name": "마슬랜 치킨",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7574-1212",
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 2,
                                    "benefit_details": []
                                },{
                                    "category_ids": [
                                       \s
                                    ],
                                    "delivery": true,
                                    "id": 2,
                                    "name": "티바",
                                    "pay_bank": true,
                                    "pay_card": true,
                                    "phone": "010-7788-9900",
                                    "is_event": false,
                                    "is_open": %s,
                                    "average_rate": 4.0,
                                    "review_count": 1,
                                    "benefit_details": ["무료", "콜라"]
                                }
                            ]
                        }
                        """, 티바_영업여부, 마슬랜_영업여부)));
    }
}
