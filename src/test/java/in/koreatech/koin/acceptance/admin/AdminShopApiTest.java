package in.koreatech.koin.acceptance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Set;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.admin.shop.repository.menu.AdminMenuCategoryRepository;
import in.koreatech.koin.admin.shop.repository.menu.AdminMenuRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopCategoryRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopParentCategoryRepository;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.menu.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.menu.MenuImage;
import in.koreatech.koin.domain.shop.model.menu.MenuOption;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import in.koreatech.koin.acceptance.fixture.MenuCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.MenuAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopNotificationMessageAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopParentCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import jakarta.persistence.EntityManager;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminShopApiTest extends AcceptanceTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AdminShopCategoryRepository adminShopCategoryRepository;

    @Autowired
    private AdminShopParentCategoryRepository adminShopParentCategoryRepository;

    @Autowired
    private AdminShopRepository adminShopRepository;

    @Autowired
    private AdminMenuRepository adminMenuRepository;

    @Autowired
    private AdminMenuCategoryRepository adminMenuCategoryRepository;

    @Autowired
    private MenuAcceptanceFixture menuFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private ShopAcceptanceFixture shopFixture;

    @Autowired
    private ShopCategoryAcceptanceFixture shopCategoryFixture;

    @Autowired
    private ShopParentCategoryAcceptanceFixture shopParentCategoryFixture;

    @Autowired
    private ShopNotificationMessageAcceptanceFixture shopNotificationMessageFixture;

    @Autowired
    private MenuCategoryAcceptanceFixture menuCategoryFixture;

    private Owner owner_현수;
    private Owner owner_준영;
    private Shop shop_마슬랜;
    private Admin admin;
    private String token_admin;
    private ShopCategory shopCategory_치킨;
    private ShopCategory shopCategory_일반;
    private MenuCategory menuCategory_메인;
    private MenuCategory menuCategory_사이드;
    private ShopParentCategory shopParentCategory_가게;
    private ShopParentCategory shopParentCategory_콜벤;
    private ShopNotificationMessage notificationMessage_가게;
    private ShopNotificationMessage notificationMessage_콜벤;

    @BeforeAll
    void setUp() {
        clear();
        admin = userFixture.코인_운영자();
        token_admin = userFixture.getToken(admin.getUser());
        owner_현수 = userFixture.현수_사장님();
        owner_준영 = userFixture.준영_사장님();
        notificationMessage_가게 = shopNotificationMessageFixture.알림메시지_가게();
        notificationMessage_콜벤 = shopNotificationMessageFixture.알림메시지_콜벤();
        shopParentCategory_가게 = shopParentCategoryFixture.상위_카테고리_가게(notificationMessage_가게);
        shopParentCategory_콜벤 = shopParentCategoryFixture.상위_카테고리_콜벤(notificationMessage_콜벤);
        shopCategory_치킨 = shopCategoryFixture.카테고리_치킨(shopParentCategory_가게);
        shopCategory_일반 = shopCategoryFixture.카테고리_일반음식(shopParentCategory_콜벤);
        shop_마슬랜 = shopFixture.마슬랜(owner_현수, shopCategory_치킨);
        menuCategory_메인 = menuCategoryFixture.메인메뉴(shop_마슬랜);
        menuCategory_사이드 = menuCategoryFixture.사이드메뉴(shop_마슬랜);
    }

    @Test
    void 어드민이_모든_상점을_조회한다() throws Exception {
        for (int i = 0; i < 12; i++) {
            Shop request = shopFixture.builder()
                .owner(owner_현수)
                .name("상점" + i)
                .internalName("상점" + i)
                .phone("010-1234-567" + i)
                .address("주소" + i)
                .description("설명" + i)
                .delivery(true)
                .deliveryPrice(1000 + i)
                .payCard(true)
                .payBank(true)
                .isDeleted(false)
                .isEvent(false)
                .remarks("비고" + i)
                .hit(0)
                .build();
            adminShopRepository.save(request);
        }

        mockMvc.perform(
                get("/admin/shops")
                    .header("Authorization", "Bearer " + token_admin)
                    .param("page", "1")
                    .param("is_deleted", "false")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(13))
            .andExpect(jsonPath("$.current_count").value(10))
            .andExpect(jsonPath("$.total_page").value(2))
            .andExpect(jsonPath("$.current_page").value(1))
            .andExpect(jsonPath("$.shops.length()").value(10));
    }

    @Test
    void 어드민이_특정_상점을_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/shops/{shopId}", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
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
                             "id": 1,
                             "name": "메인 메뉴"
                         },
                         {
                             "id": 2,
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
                       
                     ],
                     "main_category_id": 1,
                     "updated_at": "2024-01-15",
                     "is_deleted": false,
                     "is_event": false,
                     "bank": "국민",
                     "account_number": "01022595923"
                 }
                """));
    }

    @Test
    void 어드민이_상점의_등록된_순서가_아닌_정렬된_모든_카테고리를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/shops/categories")
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].name").value("일반음식점"))
            .andExpect(jsonPath("$[1].id").value(1))
            .andExpect(jsonPath("$[1].name").value("치킨"));
    }

    @Test
    void 어드민이_상점의_특정_카테고리를_조회한다() throws Exception {
        ShopCategory shopCategory = shopCategoryFixture.카테고리_치킨(shopParentCategory_가게);

        mockMvc.perform(
                get("/admin/shops/categories/{id}", shopCategory.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "id": 3,
                  "image_url": "https://test-image.com/ckicken.jpg",
                  "name": "치킨",
                  "parent_category_id": 1
                }
                """));
    }

    @Test
    void 어드민이_상점의_모든_상위_카테고리를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/shops/parent-categories")
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                         "id": 1,
                         "name": "가게"
                    },
                    {
                         "id": 2,
                         "name": "콜벤"
                    }
                ]
                """));
    }

    @Test
    void 어드민이_특정_상점의_모든_메뉴를_조회한다() throws Exception {
        // given
        menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);

        mockMvc.perform(
                get("/admin/shops/{id}/menus", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "count": 1,
                     "menu_categories": [
                         {
                             "id": 1,
                             "name": "메인 메뉴",
                             "menus": [
                                 {
                                     "id": 1,
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
    void 어드민이_특정_상점의_메뉴_카테고리들을_조회한다() throws Exception {
        // given
        menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        mockMvc.perform(
                get("/admin/shops/{id}/menus/categories", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "count": 2,
                    "menu_categories": [
                        {
                            "id": 1,
                            "name": "메인 메뉴"
                        },
                        {
                            "id": 2,
                            "name": "사이드 메뉴"
                        }
                    ]
                }
                """));
    }

    @Test
    void 어드민이_특정_상점의_특정_메뉴를_조회한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);

        mockMvc.perform(
                get("/admin/shops/{shopId}/menus/{menuId}", menu.getId(), shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "category_ids": [1],
                    "description": "맛있는 짜장면",
                    "id": 1,
                    "image_urls": [
                        "https://test.com/짜장면.jpg",
                        "https://test.com/짜장면22.jpg"
                    ],
                    "is_hidden": false,
                    "is_single": false,
                    "name": "짜장면",
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
                    "shop_id": 1,
                    "single_price": null
                }
                """));
    }

    @Test
    void 어드민이_상점을_생성한다() throws Exception {
        mockMvc.perform(
                post("/admin/shops")
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                            "address": "대전광역시 유성구 대학로 291",
                            "main_category_id": 1,
                            "category_ids": [
                                %d
                            ],
                            "delivery": true,
                            "delivery_price": 4000,
                            "description": "테스트 상점2입니다.",
                            "image_urls": [
                                "https://test.com/test1.jpg",
                                "https://test.com/test2.jpg",
                                "https://test.com/test3.jpg"
                            ],
                            "name": "테스트 상점2",
                            "open": [
                                {
                                    "close_time": "21:00",
                                    "closed": false,
                                    "day_of_week": "MONDAY",
                                    "open_time": "09:00"
                                },
                                {
                                    "close_time": "21:00",
                                    "closed": false,
                                    "day_of_week": "TUESDAY",
                                    "open_time": "09:00"
                                },
                                {
                                    "close_time": "21:00",
                                    "closed": false,
                                    "day_of_week": "WEDNESDAY",
                                    "open_time": "09:00"
                                },
                                {
                                    "close_time": "21:00",
                                    "closed": false,
                                    "day_of_week": "THURSDAY",
                                    "open_time": "09:00"
                                },
                                {
                                    "close_time": "21:00",
                                    "closed": false,
                                    "day_of_week": "FRIDAY",
                                    "open_time": "09:00"
                                },
                                {
                                    "close_time": "21:00",
                                    "closed": false,
                                    "day_of_week": "SATURDAY",
                                    "open_time": "09:00"
                                },
                                {
                                    "close_time": "21:00",
                                    "closed": false,
                                    "day_of_week": "SUNDAY",
                                    "open_time": "09:00"
                                }
                            ],
                            "pay_bank": true,
                            "pay_card": true,
                            "phone": "010-1234-5678"
                        }
                        """, shopCategory_치킨.getId()))
            )
            .andExpect(status().isCreated());

        Shop savedShop = adminShopRepository.getById(2);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(savedShop.getAddress()).isEqualTo("대전광역시 유성구 대학로 291");
            softly.assertThat(savedShop.getDeliveryPrice()).isEqualTo(4000);
            softly.assertThat(savedShop.getDescription()).isEqualTo("테스트 상점2입니다.");
            softly.assertThat(savedShop.getName()).isEqualTo("테스트 상점2");
            softly.assertThat(savedShop.getShopImages()).hasSize(3);
            softly.assertThat(savedShop.getShopOpens()).hasSize(7);
            softly.assertThat(savedShop.getShopCategories()).hasSize(1);
        });
    }

    @Test
    void 어드민이_상점_카테고리를_생성한다() throws Exception {
        mockMvc.perform(
                post("/admin/shops/categories")
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "image_url": "https://image.png",
                          "name": "새로운 카테고리",
                          "parent_category_id": "1"
                        }
                        """)
            )
            .andExpect(status().isCreated());

        transactionTemplate.executeWithoutResult(status -> {
            ShopCategory result = adminShopCategoryRepository.getById(3);
            ShopParentCategory shopParentCategory = adminShopParentCategoryRepository.getById(1);
            assertSoftly(
                softly -> {
                    softly.assertThat(result.getImageUrl()).isEqualTo("https://image.png");
                    softly.assertThat(result.getName()).isEqualTo("새로운 카테고리");
                    softly.assertThat(result.getParentCategory()).isEqualTo(shopParentCategory);
                }
            );
        });
    }

    @Test
    void 어드민이_옵션이_여러개인_메뉴를_추가한다() throws Exception {
        // given
        MenuCategory menuCategory = menuCategory_메인;

        mockMvc.perform(
                post("/admin/shops/{id}/menus", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                          "category_ids": [
                            %s
                          ],
                          "description": "테스트메뉴입니다.",
                          "image_urls": [
                            "https://test-image.com/짜장면.jpg"
                          ],
                          "is_single": false,
                          "name": "짜장면",
                          "option_prices": [
                            {
                              "option": "중",
                              "price": 10000
                            },
                            {
                              "option": "소",
                              "price": 5000
                            }
                          ]
                        }
                        """, menuCategory.getId()))
            )
            .andExpect(status().isCreated());

        Menu menu = adminMenuRepository.getById(1);
        assertSoftly(
            softly -> {
                List<MenuCategoryMap> menuCategoryMaps = menu.getMenuCategoryMaps();
                List<MenuOption> menuOptions = menu.getMenuOptions();
                List<MenuImage> menuImages = menu.getMenuImages();
                softly.assertThat(menu.getDescription()).isEqualTo("테스트메뉴입니다.");
                softly.assertThat(menu.getName()).isEqualTo("짜장면");
                softly.assertThat(menuImages.get(0).getImageUrl()).isEqualTo("https://test-image.com/짜장면.jpg");
                softly.assertThat(menuCategoryMaps.get(0).getMenuCategory().getId()).isEqualTo(1);
                softly.assertThat(menuOptions).hasSize(2);
            }
        );
    }

    @Test
    void 어드민이_옵션이_한개인_메뉴를_추가한다() throws Exception {
        // given
        MenuCategory menuCategory = menuCategory_메인;

        mockMvc.perform(
                post("/admin/shops/{id}/menus", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                          "category_ids": [
                            %s
                          ],
                          "description": "테스트메뉴입니다.",
                          "image_urls": [
                            "https://test-image.com/짜장면.jpg"
                          ],
                          "is_single": true,
                          "name": "짜장면",
                          "option_prices": null,
                          "single_price": 10000
                        }
                        """, menuCategory.getId()))
            )
            .andExpect(status().isCreated());

        Menu menu = adminMenuRepository.getById(1);
        assertSoftly(
            softly -> {
                List<MenuCategoryMap> menuCategoryMaps = menu.getMenuCategoryMaps();
                List<MenuOption> menuOptions = menu.getMenuOptions();
                List<MenuImage> menuImages = menu.getMenuImages();
                System.out.println("transaction test");
                softly.assertThat(menu.getDescription()).isEqualTo("테스트메뉴입니다.");
                softly.assertThat(menu.getName()).isEqualTo("짜장면");

                softly.assertThat(menuImages.get(0).getImageUrl()).isEqualTo("https://test-image.com/짜장면.jpg");
                softly.assertThat(menuCategoryMaps.get(0).getMenuCategory().getId()).isEqualTo(1);

                softly.assertThat(menuOptions.get(0).getPrice()).isEqualTo(10000);
            }
        );
    }

    @Test
    void 어드민이_메뉴_카테고리를_추가한다() throws Exception {
        // given
        mockMvc.perform(
                post("/admin/shops/{id}/menus/categories", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                           "name": "대박메뉴"
                        }
                        """))
            )
            .andExpect(status().isCreated());

        var menuCategories = adminMenuCategoryRepository.findAllByShopId(shop_마슬랜.getId());

        assertThat(menuCategories).anyMatch(menuCategory -> "대박메뉴".equals(menuCategory.getName()));
    }

    @Test
    void 어드민이_상점_삭제를_해제한다() throws Exception {
        // given
        adminShopRepository.deleteById(shop_마슬랜.getId());

        mockMvc.perform(
                post("/admin/shops/{id}/undelete", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        var shop = adminShopRepository.getById(shop_마슬랜.getId());
        assertSoftly(softly -> softly.assertThat(shop.isDeleted()).isFalse());
    }

    @Test
    void 어드민이_상점을_수정한다() throws Exception {
        mockMvc.perform(
                put("/admin/shops/{id}", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                          "address": "충청남도 천안시 동남구 병천면 충절로 1600",
                          "main_category_id": 2,
                          "category_ids": [
                           %d, %d
                          ],
                          "delivery": false,
                          "delivery_price": 1000,
                          "description": "이번주 전 메뉴 10%% 할인 이벤트합니다.",
                          "image_urls": [
                            "https://fixed-shopimage.com/수정된_상점_이미지.png"
                          ],
                          "name": "써니 숯불 도시락",
                          "open": [
                            {
                                "close_time": "00:00",
                                "closed": false,
                                "day_of_week": "MONDAY",
                                "open_time": "01:00"
                            },
                            {
                                "close_time": "21:00",
                                "closed": false,
                                "day_of_week": "TUESDAY",
                                "open_time": "09:00"
                            },
                            {
                                "close_time": "21:00",
                                "closed": false,
                                "day_of_week": "WEDNESDAY",
                                "open_time": "09:00"
                            },
                            {
                                "close_time": "21:00",
                                "closed": false,
                                "day_of_week": "THURSDAY",
                                "open_time": "09:00"
                            },
                            {
                                "close_time": "21:00",
                                "closed": false,
                                "day_of_week": "FRIDAY",
                                "open_time": "09:00"
                            },
                            {
                                "close_time": "21:00",
                                "closed": false,
                                "day_of_week": "SATURDAY",
                                "open_time": "09:00"
                            },
                            {
                                "close_time": "21:00",
                                "closed": false,
                                "day_of_week": "SUNDAY",
                                "open_time": "09:00"
                            }
                          ],
                          "pay_bank": true,
                          "pay_card": true,
                          "phone": "041-123-4567"
                        }
                        """, shopCategory_일반.getId(), shopCategory_치킨.getId()))
            )
            .andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            Shop result = adminShopRepository.getById(shop_마슬랜.getId());
            List<ShopImage> shopImages = result.getShopImages();
            List<ShopOpen> shopOpens = result.getShopOpens();
            Set<ShopCategoryMap> shopCategoryMaps = result.getShopCategories();

            assertSoftly(softly -> {
                softly.assertThat(result.getAddress()).isEqualTo("충청남도 천안시 동남구 병천면 충절로 1600");
                softly.assertThat(result.getShopMainCategory().getId()).isEqualTo(2);
                softly.assertThat(result.isDeleted()).isFalse();
                softly.assertThat(result.getDeliveryPrice()).isEqualTo(1000);
                softly.assertThat(result.getDescription()).isEqualTo("이번주 전 메뉴 10% 할인 이벤트합니다.");
                softly.assertThat(result.getName()).isEqualTo("써니 숯불 도시락");
                softly.assertThat(result.isPayBank()).isTrue();
                softly.assertThat(result.isPayCard()).isTrue();
                softly.assertThat(result.getPhone()).isEqualTo("041-123-4567");

                softly.assertThat(shopCategoryMaps.size()).isEqualTo(2);

                softly.assertThat(shopImages.size()).isEqualTo(1);
                softly.assertThat(shopImages.get(0).getImageUrl())
                    .isEqualTo("https://fixed-shopimage.com/수정된_상점_이미지.png");

                softly.assertThat(shopOpens.size()).isEqualTo(7);
                softly.assertThat(shopOpens.get(0).getCloseTime()).isEqualTo("00:00");
                softly.assertThat(shopOpens.get(0).getOpenTime()).isEqualTo("01:00");
                softly.assertThat(shopOpens.get(0).getDayOfWeek()).isEqualTo("MONDAY");
                softly.assertThat(shopOpens.get(0).isClosed()).isFalse();
            });
        });
    }

    @Test
    void 어드민이_상점_카테고리를_수정한다() throws Exception {
        ShopCategory shopCategory = shopCategoryFixture.카테고리_일반음식(shopParentCategory_가게);

        mockMvc.perform(
                put("/admin/shops/categories/{id}", shopCategory.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "image_url": "http://image.png",
                          "name": "수정된 카테고리 이름",
                          "parent_category_id": "2"
                        }
                        """)
            )
            .andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            ShopCategory updatedCategory = adminShopCategoryRepository.getById(shopCategory.getId());
            ShopParentCategory shopParentCategory = adminShopParentCategoryRepository.getById(2);
            assertSoftly(
                softly -> {
                    softly.assertThat(updatedCategory.getId()).isEqualTo(shopCategory.getId());
                    softly.assertThat(updatedCategory.getImageUrl()).isEqualTo("http://image.png");
                    softly.assertThat(updatedCategory.getName()).isEqualTo("수정된 카테고리 이름");
                    softly.assertThat(updatedCategory.getParentCategory()).isEqualTo(shopParentCategory);
                }
            );
        });
    }

    @Test
    void 어드민이_특정_상점의_메뉴_카테고리를_수정한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);
        mockMvc.perform(
                put("/admin/shops/{shopId}/menus/categories", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                           "id": %s,
                           "name": "사이드 메뉴"
                        }
                        """, menuCategory_메인.getId()))
            )
            .andExpect(status().isCreated());

        MenuCategory menuCategory = adminMenuCategoryRepository.getById(menuCategory_메인.getId());
        assertSoftly(softly -> softly.assertThat(menuCategory.getName()).isEqualTo("사이드 메뉴"));
    }

    @Test
    void 어드민이_상점_카테고리_순서를_변경한다() throws Exception {
        mockMvc.perform(
                put("/admin/shops/categories/order")
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "shop_category_ids": [%d, %d]
                        }
                        """.formatted(shopCategory_치킨.getId(), shopCategory_일반.getId()))
            )
            .andExpect(status().isNoContent());

        List<ShopCategory> shopCategories = adminShopCategoryRepository.findAll(Sort.by("orderIndex"));
        assertSoftly(softly -> {
            softly.assertThat(shopCategories.get(0).getId()).isEqualTo(shopCategory_치킨.getId());
            softly.assertThat(shopCategories.get(0).getOrderIndex()).isEqualTo(0);
            softly.assertThat(shopCategories.get(1).getId()).isEqualTo(shopCategory_일반.getId());
            softly.assertThat(shopCategories.get(1).getOrderIndex()).isEqualTo(1);
        });
    }

    @Test
    void 어드민이_특정_상점의_메뉴를_단일_메뉴로_수정한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        mockMvc.perform(
                put("/admin/shops/{shopId}/menus/{menuId}", shop_마슬랜.getId(), menu.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                          "category_ids": [
                            %d
                          ],
                          "description": "테스트메뉴수정",
                          "image_urls": [
                            "https://test-image.net/테스트메뉴.jpeg"
                          ],
                          "is_single": true,
                          "name": "짜장면2",
                          "single_price": 10000
                        }
                        """, shopCategory_일반.getId()))
            )
            .andExpect(status().isCreated());

        transactionTemplate.executeWithoutResult(status -> {
            Menu result = adminMenuRepository.getById(1);
            assertSoftly(
                softly -> {
                    List<MenuCategoryMap> menuCategoryMaps = result.getMenuCategoryMaps();
                    List<MenuOption> menuOptions = result.getMenuOptions();
                    List<MenuImage> menuImages = result.getMenuImages();
                    softly.assertThat(result.getDescription()).isEqualTo("테스트메뉴수정");
                    softly.assertThat(result.getName()).isEqualTo("짜장면2");

                    softly.assertThat(menuImages.get(0).getImageUrl()).isEqualTo("https://test-image.net/테스트메뉴.jpeg");
                    softly.assertThat(menuCategoryMaps.get(0).getMenuCategory().getId()).isEqualTo(2);

                    softly.assertThat(menuOptions.get(0).getPrice()).isEqualTo(10000);

                }
            );
        });
    }

    @Test
    void 어드민이_특정_상점의_메뉴를_여러옵션을_가진_메뉴로_수정한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);
        mockMvc.perform(
                put("/admin/shops/{shopId}/menus/{menuId}", shop_마슬랜.getId(), menu.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                          "category_ids": [
                            %d, %d
                          ],
                          "description": "테스트메뉴입니다.",
                          "image_urls": [
                            "https://fixed-testimage.com/수정된짜장면.png"
                          ],
                          "is_single": false,
                          "name": "짜장면",
                          "option_prices": [
                            {
                              "option": "중",
                              "price": 10000
                            },
                            {
                              "option": "소",
                              "price": 5000
                            }
                          ]
                        }
                        """, menuCategory_메인.getId(), menuCategory_사이드.getId()))
            )
            .andExpect(status().isCreated());

        transactionTemplate.executeWithoutResult(status -> {
            Menu result = adminMenuRepository.getById(1);
            assertSoftly(
                softly -> {
                    List<MenuCategoryMap> menuCategoryMaps = result.getMenuCategoryMaps();
                    List<MenuOption> menuOptions = result.getMenuOptions();
                    List<MenuImage> menuImages = result.getMenuImages();
                    softly.assertThat(result.getDescription()).isEqualTo("테스트메뉴입니다.");
                    softly.assertThat(result.getName()).isEqualTo("짜장면");
                    softly.assertThat(menuImages.get(0).getImageUrl())
                        .isEqualTo("https://fixed-testimage.com/수정된짜장면.png");
                    softly.assertThat(menuCategoryMaps).hasSize(2);
                    softly.assertThat(menuOptions).hasSize(2);
                }
            );
        });
    }

    @Test
    void 어드민이_특정_상점의_메뉴를_여러옵션을_가진_메뉴로_수정한다_가격_옵션이_비어있거나_null이면_400_에러_발생() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);

        mockMvc.perform(
                put("/admin/shops/{shopId}/menus/{menuId}", shop_마슬랜.getId(), menu.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                          "category_ids": [
                            %d, %d
                          ],
                          "description": "테스트메뉴입니다.",
                          "image_urls": [
                            "https://fixed-testimage.com/수정된짜장면.png"
                          ],
                          "is_single": false,
                          "name": "짜장면",
                          "option_prices": []
                        }
                        """, menuCategory_메인.getId(), menuCategory_사이드.getId()))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 어드민이_상점을_삭제한다() throws Exception {
        Shop shop = shopFixture.영업중이_아닌_신전_떡볶이(owner_현수);

        mockMvc.perform(
                delete("/admin/shops/{id}", shop.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isOk());

        Shop deletedShop = adminShopRepository.getById(shop.getId());
        assertSoftly(softly -> softly.assertThat(deletedShop.isDeleted()).isTrue());
    }

    @Test
    void 어드민이_상점_카테고리를_삭제한다() throws Exception {
        ShopCategory shopCategory = shopCategoryFixture.카테고리_치킨(shopParentCategory_가게);

        mockMvc.perform(
                delete("/admin/shops/categories/{id}", shopCategory.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isNoContent());

        assertThat(adminMenuCategoryRepository.findById(shopCategory.getId())).isNotPresent();
    }

    @Test
    void 어드민이_상점_카테고리_삭제시_카테고리에_상점이_남아있으면_400() throws Exception {
        ShopCategoryMap shopCategoryMap = ShopCategoryMap.builder()
            .shop(shop_마슬랜)
            .shopCategory(shopCategory_치킨)
            .build();
        entityManager.persist(shopCategoryMap);

        mockMvc.perform(
                delete("/admin/shops/categories/{id}", shopCategory_치킨.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 어드민이_특정_상점의_메뉴_카테고리를_삭제한다() throws Exception {
        // when & then
        mockMvc.perform(
                delete(
                    "/admin/shops/{shopId}/menus/categories/{categoryId}",
                    shop_마슬랜.getId(),
                    menuCategory_메인.getId()
                )
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isNoContent());

        assertThat(adminMenuCategoryRepository.findById(menuCategory_메인.getId())).isNotPresent();
    }

    @Test
    void 어드민이_메뉴를_삭제한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        mockMvc.perform(
                delete("/admin/shops/{shopId}/menus/{menuId}", shop_마슬랜.getId(), menu.getId())
                    .header("Authorization", "Bearer " + token_admin)
            )
            .andExpect(status().isNoContent());

        assertThat(adminMenuRepository.findById(menu.getId())).isNotPresent();
    }
}
