package in.koreatech.koin.acceptance.domain;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.notification.eventlistener.ShopEventListener;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.event.EventArticle;
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
import in.koreatech.koin.domain.shop.repository.event.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.menu.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.menu.MenuRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.acceptance.fixture.EventArticleAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.MenuCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.MenuAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopNotificationMessageAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopParentCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import jakarta.transaction.Transactional;

@Transactional
@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnerShopApiTest extends AcceptanceTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private EventArticleRepository eventArticleRepository;

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
    private ShopNotificationMessageAcceptanceFixture shopNotificationMessageFixture;;

    @Autowired
    private MenuCategoryAcceptanceFixture menuCategoryFixture;

    @Autowired
    private EventArticleAcceptanceFixture eventArticleFixture;

    @MockBean
    private ShopEventListener shopEventListener;

    private Owner owner_현수;
    private String token_현수;
    private Owner owner_준영;
    private String token_준영;
    private Shop shop_마슬랜;
    private ShopNotificationMessage notificationMessage_가게;
    private ShopParentCategory shopParentCategory_가게;
    private ShopCategory shopCategory_치킨;
    private ShopCategory shopCategory_일반;
    private MenuCategory menuCategory_메인;
    private MenuCategory menuCategory_사이드;

    @BeforeAll
    void setUp() {
        clear();
        owner_현수 = userFixture.현수_사장님();
        token_현수 = userFixture.getToken(owner_현수.getUser());
        owner_준영 = userFixture.준영_사장님();
        token_준영 = userFixture.getToken(owner_준영.getUser());
        notificationMessage_가게 = shopNotificationMessageFixture.알림메시지_가게();
        shopParentCategory_가게 = shopParentCategoryFixture.상위_카테고리_가게(notificationMessage_가게);
        shopCategory_치킨 = shopCategoryFixture.카테고리_치킨(shopParentCategory_가게);
        shopCategory_일반 = shopCategoryFixture.카테고리_일반음식(shopParentCategory_가게);
        shop_마슬랜 = shopFixture.마슬랜(owner_현수, shopCategory_치킨);
        menuCategory_메인 = menuCategoryFixture.메인메뉴(shop_마슬랜);
        menuCategory_사이드 = menuCategoryFixture.사이드메뉴(shop_마슬랜);
    }

    @Test
    void 사장님의_가게_목록을_조회한다() throws Exception {
        // given
        shopFixture.배달_안되는_신전_떡볶이(owner_현수);
        mockMvc.perform(
                get("/owner/shops")
                    .header("Authorization", "Bearer " + token_현수)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "count": 2,
                    "shops": [
                        {
                            "id": 1,
                            "name": "마슬랜 치킨",
                            "is_event": false
                        },
                        {
                            "id": 2,
                            "name": "신전 떡볶이",
                            "is_event": false
                        }
                    ]
                }
                """)
            );
    }

    @Test
    void 상점을_생성한다() throws Exception {
        // given
        mockMvc.perform(
                post("/owner/shops")
                    .header("Authorization", "Bearer " + token_현수)
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
                        """, shopCategory_치킨.getId())
                    )
            )
            .andExpect(status().isCreated());
        List<Shop> shops = shopRepository.findAllByOwnerId(owner_현수.getId());
        Shop result = shops.get(1);
        assertSoftly(
            softly -> {
                softly.assertThat(result.getAddress()).isEqualTo("대전광역시 유성구 대학로 291");
                softly.assertThat(result.getDeliveryPrice()).isEqualTo(4000);
                softly.assertThat(result.getDescription()).isEqualTo("테스트 상점2입니다.");
                softly.assertThat(result.getName()).isEqualTo("테스트 상점2");
                softly.assertThat(result.getShopImages()).hasSize(3);
                softly.assertThat(result.getShopOpens()).hasSize(7);
                softly.assertThat(result.getShopCategories()).hasSize(1);
                System.out.println("dsa");
            }
        );
    }

    @Test
    void 상점_사장님이_특정_상점_조회() throws Exception {
        mockMvc.perform(
                get("/owner/shops/{shopId}", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_현수)
            ).andExpect(status().isOk())
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
                     "main_category_id": 1,
                     "shop_categories": [
                     ],
                     "updated_at": "2024-01-15",
                     "is_event": false,
                     "bank": "국민",
                     "account_number": "01022595923"
                 }
                """));
    }

    @Test
    void 특정_상점의_모든_메뉴를_조회한다() throws Exception {
        menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);
        mockMvc.perform(
                get("/owner/shops/menus")
                    .header("Authorization", "Bearer " + token_현수)
                    .param("shopId", shop_마슬랜.getId().toString())
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
    void 사장님이_자신의_상점_메뉴_카테고리들을_조회한다() throws Exception {
        // given
        menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);
        mockMvc.perform(
                get("/owner/shops/menus/categories")
                    .header("Authorization", "Bearer " + token_현수)
                    .param("shopId", shop_마슬랜.getId().toString())
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
    void 사장님이_자신의_상점의_특정_메뉴를_조회한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);
        mockMvc.perform(
                get("/owner/shops/menus/{menuId}", menu.getId())
                    .header("Authorization", "Bearer " + token_현수)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(menu.getId()))
            .andExpect(jsonPath("$.shop_id").value(menu.getShop().getId()))
            .andExpect(jsonPath("$.name").value(menu.getName()))
            .andExpect(jsonPath("$.is_hidden").value(menu.isHidden()))
            .andExpect(jsonPath("$.is_single").value(false))
            .andExpect(jsonPath("$.single_price").doesNotExist())
            .andExpect(jsonPath("$.option_prices", hasSize(2)))
            .andExpect(jsonPath("$.description").value(menu.getDescription()))
            .andExpect(jsonPath("$.category_ids", hasSize(menu.getMenuCategoryMaps().size())))
            .andExpect(jsonPath("$.image_urls", hasSize(menu.getMenuImages().size())));
    }

    @Test
    void 권한이_없는_상점_사장님이_특정_상점_조회() throws Exception {
        // given
        mockMvc.perform(
                get("/owner/shops/{shopId}", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_준영)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 사장님이_메뉴_카테고리를_삭제한다() throws Exception {
        // when & then
        mockMvc.perform(
                delete("/owner/shops/menus/categories/{categoryId}", menuCategory_메인.getId())
                    .header("Authorization", "Bearer " + token_현수)
            )
            .andExpect(status().isNoContent());
        assertThat(menuCategoryRepository.findById(menuCategory_메인.getId())).isNotPresent();
    }

    @Test
    void 사장님이_메뉴를_삭제한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);
        mockMvc.perform(
                delete("/owner/shops/menus/{menuId}", menu.getId())
                    .header("Authorization", "Bearer " + token_현수)
            )
            .andExpect(status().isNoContent());
        assertThat(menuRepository.findById(menu.getId())).isNotPresent();
    }

    @Test
    void 사장님이_옵션이_여러개인_메뉴를_추가한다() throws Exception {
        // given
        MenuCategory menuCategory = menuCategory_메인;
        mockMvc.perform(
                post("/owner/shops/{id}/menus", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_현수)
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
        Menu menu = menuRepository.getById(1);
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
    void 사장님이_옵션이_한개인_메뉴를_추가한다() throws Exception {
        // given
        MenuCategory menuCategory = menuCategory_메인;
        mockMvc.perform(
                post("/owner/shops/{id}/menus", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_현수)
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
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());
        transactionTemplate.executeWithoutResult(execute -> {
            Menu menu = menuRepository.getById(1);
            assertSoftly(
                softly -> {
                    List<MenuCategoryMap> menuCategoryMaps = menu.getMenuCategoryMaps();
                    List<MenuOption> menuOptions = menu.getMenuOptions();
                    List<MenuImage> menuImages = menu.getMenuImages();
                    softly.assertThat(menu.getDescription()).isEqualTo("테스트메뉴입니다.");
                    softly.assertThat(menu.getName()).isEqualTo("짜장면");
                    softly.assertThat(menuImages.get(0).getImageUrl()).isEqualTo("https://test-image.com/짜장면.jpg");
                    softly.assertThat(menuCategoryMaps.get(0).getMenuCategory().getId()).isEqualTo(1);
                    softly.assertThat(menuOptions.get(0).getPrice()).isEqualTo(10000);
                }
            );
        });
    }

    @Test
    void 사장님이_메뉴_카테고리를_추가한다() throws Exception {
        menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);
        mockMvc.perform(
                post("/owner/shops/{id}/menus/categories", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_현수)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                           "name": "대박메뉴"
                        }
                        """)
            )
            .andExpect(status().isCreated());
        var menuCategories = menuCategoryRepository.findAllByShopId(shop_마슬랜.getId());
        assertThat(menuCategories).anyMatch(menuCategory -> "대박메뉴".equals(menuCategory.getName()));
    }

    @Test
    void 사장님이_단일_메뉴로_수정한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        mockMvc.perform(
                put("/owner/shops/menus/{menuId}", menu.getId())
                    .header("Authorization", "Bearer " + token_현수)
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
            Menu result = menuRepository.getById(1);
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
    void 사장님이_여러옵션을_가진_메뉴로_수정한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);

        mockMvc.perform(
                put("/owner/shops/menus/{menuId}", menu.getId())
                    .header("Authorization", "Bearer " + token_현수)
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
                        """, menuCategory_메인.getId(), menuCategory_사이드.getId())
                    )
            )
            .andExpect(status().isCreated());

        transactionTemplate.executeWithoutResult(status -> {
            Menu result = menuRepository.getById(1);
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
    void 사장님이_상점을_수정한다() throws Exception {
        // given
        mockMvc.perform(
                put("/owner/shops/{id}", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_현수)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                            {
                              "address": "충청남도 천안시 동남구 병천면 충절로 1600",
                              "main_category_id": 1,
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
                                  "close_time": "22:30",
                                  "closed": false,
                                  "day_of_week": "MONDAY",
                                  "open_time": "10:00"
                                },
                                {
                                  "close_time": "23:30",
                                  "closed": true,
                                  "day_of_week": "SUNDAY",
                                  "open_time": "11:00"
                                }
                              ],
                              "pay_bank": true,
                              "pay_card": true,
                              "phone": "041-123-4567"
                            }
                            """, shopCategory_일반.getId(), shopCategory_치킨.getId()
                        )
                    )
            )
            .andExpect(status().isCreated());

        transactionTemplate.executeWithoutResult(status -> {
            Shop result = shopRepository.getById(1);
            List<ShopImage> shopImages = result.getShopImages();
            List<ShopOpen> shopOpens = result.getShopOpens();
            Set<ShopCategoryMap> shopCategoryMaps = result.getShopCategories();
            assertSoftly(
                softly -> {
                    softly.assertThat(result.getAddress()).isEqualTo("충청남도 천안시 동남구 병천면 충절로 1600");
                    softly.assertThat(result.getShopMainCategory().getId()).isEqualTo(1);
                    softly.assertThat(result.isDeleted()).isFalse();
                    softly.assertThat(result.getDeliveryPrice()).isEqualTo(1000);
                    softly.assertThat(result.getDescription()).isEqualTo("이번주 전 메뉴 10% 할인 이벤트합니다.");
                    softly.assertThat(result.getName()).isEqualTo("써니 숯불 도시락");
                    softly.assertThat(result.isPayBank()).isTrue();
                    softly.assertThat(result.isPayCard()).isTrue();
                    softly.assertThat(result.getPhone()).isEqualTo("041-123-4567");

                    softly.assertThat(shopCategoryMaps.size()).isEqualTo(2);

                    softly.assertThat(shopImages.get(0).getImageUrl())
                        .isEqualTo("https://fixed-shopimage.com/수정된_상점_이미지.png");

                    softly.assertThat(shopOpens.get(0).getCloseTime()).isEqualTo("22:30");
                    softly.assertThat(shopOpens.get(0).getOpenTime()).isEqualTo("10:00");

                    softly.assertThat(shopOpens.get(0).getDayOfWeek()).isEqualTo("MONDAY");
                    softly.assertThat(shopOpens.get(0).isClosed()).isFalse();

                    softly.assertThat(shopOpens.get(1).getCloseTime()).isEqualTo("23:30");
                    softly.assertThat(shopOpens.get(1).getOpenTime()).isEqualTo("11:00");

                    softly.assertThat(shopOpens.get(1).getDayOfWeek()).isEqualTo("SUNDAY");
                    softly.assertThat(shopOpens.get(1).isClosed()).isTrue();
                }
            );
        });
    }

    @Test
    void 사장님이_단일_메뉴로_수정한다_가격_옵션이_null이_아니면_400_에러() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        mockMvc.perform(
                put("/owner/shops/menus/{menuId}", menu.getId())
                    .header("Authorization", "Bearer " + token_현수)
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
                          "single_price": 10000,
                          "option_prices": []
                        }
                        """, shopCategory_일반.getId())
                    )
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 권한이_없는_상점_사장님이_특정_카테고리_조회한다() throws Exception {
        // given
        mockMvc.perform(
                get("/owner/shops/menus/categories")
                    .header("Authorization", "Bearer " + token_준영)
                    .param("shopId", shop_마슬랜.getId().toString())
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 권한이_없는_상점_사장님이_특정_메뉴_조회한다() throws Exception {
        menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);
        // given
        mockMvc.perform(
                get("/owner/shops/menus/{menuId}", 1)
                    .header("Authorization", "Bearer " + token_준영)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 권한이_없는_사장님이_메뉴_카테고리를_삭제한다() throws Exception {
        // given
        MenuCategory menuCategory = menuCategoryFixture.세트메뉴(shop_마슬랜);
        mockMvc.perform(
                delete("/owner/shops/menus/categories/{categoryId}", menuCategory.getId())
                    .header("Authorization", "Bearer " + token_준영)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 권한이_없는_사장님이_메뉴를_삭제한다() throws Exception {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);
        mockMvc.perform(
                delete("/owner/shops/menus/{menuId}", menu.getId())
                    .header("Authorization", "Bearer " + token_준영)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 사장님이_이벤트를_추가한다() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(10);
        transactionTemplate.executeWithoutResult(status -> {
            try {
                mockMvc.perform(
                        post("/owner/shops/{shopId}/event", shop_마슬랜.getId())
                            .header("Authorization", "Bearer " + token_현수)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                        {
                                          "title": "감성떡볶이 이벤트합니다!",
                                          "content": "테스트 이벤트입니다.",
                                          "thumbnail_images": [
                                            "https://test.com/test1.jpg"
                                          ],
                                          "start_date": "%s",
                                          "end_date": "%s"
                                        }
                                        """,
                                    startDate.format(ofPattern("yyyy-MM-dd")),
                                    endDate.format(ofPattern("yyyy-MM-dd"))
                                )
                            )
                    )
                    .andExpect(status().isCreated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        EventArticle eventArticle = eventArticleRepository.getById(1);
        assertSoftly(
            softly -> {
                softly.assertThat(eventArticle.getShop().getId()).isEqualTo(1);
                softly.assertThat(eventArticle.getTitle()).isEqualTo("감성떡볶이 이벤트합니다!");
                softly.assertThat(eventArticle.getContent()).isEqualTo("테스트 이벤트입니다.");
                softly.assertThat(eventArticle.getThumbnailImages().get(0).getThumbnailImage())
                    .isEqualTo("https://test.com/test1.jpg");
                softly.assertThat(eventArticle.getStartDate()).isEqualTo(startDate);
                softly.assertThat(eventArticle.getEndDate()).isEqualTo(endDate);
            }
        );
        forceVerify(() -> verify(shopEventListener, times(1)).onShopEventCreate(any()));
        clear();
        setUp();
    }

    @Test
    void 사장님이_이벤트를_수정한다() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(10);
        EventArticle eventArticle = eventArticleFixture.할인_이벤트(shop_마슬랜, startDate, endDate);
        mockMvc.perform(
                put("/owner/shops/{shopId}/events/{eventId}", shop_마슬랜.getId(), eventArticle.getId())
                    .header("Authorization", "Bearer " + token_현수)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                            {
                              "title": "감성떡볶이 이벤트합니다!",
                              "content": "테스트 이벤트입니다.",
                              "thumbnail_images": [
                                "https://test.com/test1.jpg"
                              ],
                              "start_date": "%s",
                              "end_date": "%s"
                            }
                            """,
                        startDate,
                        endDate)
                    )
            )
            .andExpect(status().isCreated());

        EventArticle result = eventArticleRepository.getById(eventArticle.getId());
        assertSoftly(
            softly -> {
                softly.assertThat(result.getShop().getId()).isEqualTo(1);
                softly.assertThat(result.getTitle()).isEqualTo("감성떡볶이 이벤트합니다!");
                softly.assertThat(result.getContent()).isEqualTo("테스트 이벤트입니다.");
                softly.assertThat(result.getThumbnailImages().get(0).getThumbnailImage())
                    .isEqualTo("https://test.com/test1.jpg");
                softly.assertThat(result.getStartDate()).isEqualTo(startDate);
                softly.assertThat(result.getEndDate()).isEqualTo(endDate);
            }
        );
    }

    @Test
    void 사장님이_이벤트를_삭제한다() throws Exception {
        EventArticle eventArticle = eventArticleFixture.할인_이벤트(
            shop_마슬랜,
            LocalDate.of(2024, 10, 24),
            LocalDate.of(2024, 10, 26)
        );
        mockMvc.perform(
                delete("/owner/shops/{shopId}/events/{eventId}", shop_마슬랜.getId(), eventArticle.getId())
                    .header("Authorization", "Bearer " + token_현수)
            )
            .andExpect(status().isNoContent());
        Optional<EventArticle> modifiedEventArticle = eventArticleRepository.findById(eventArticle.getId());
        assertThat(modifiedEventArticle).isNotPresent();
    }

    @Test
    void 이미지_url의_요소가_공백인_채로_상점을_수정하면_400에러가_반환된다() throws Exception {
        // given
        mockMvc.perform(
                put("/owner/shops/{shopId}", shop_마슬랜.getId())
                    .header("Authorization", "Bearer " + token_현수)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                        {
                          "address": "충청남도 천안시 동남구 병천면 충절로 1600",
                          "category_ids": [
                           %d, %d
                          ],
                          "delivery": false,
                          "delivery_price": 1000,
                          "description": "이번주 전 메뉴 10%% 할인 이벤트합니다.",
                          "image_urls": [
                            ""
                          ],
                          "name": "써니 숯불 도시락",
                          "open": [
                            {
                              "close_time": "22:30",
                              "closed": false,
                              "day_of_week": "MONDAY",
                              "open_time": "10:00"
                            },
                            {
                              "close_time": "23:30",
                              "closed": true,
                              "day_of_week": "SUNDAY",
                              "open_time": "11:00"
                            }
                          ],
                          "pay_bank": true,
                          "pay_card": true,
                          "phone": "041-123-4567"
                        }
                        """, shopCategory_일반.getId(), shopCategory_치킨.getId()
                    )))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 이미지_url의_요소가_공백인_채로_상점을_생성하면_400에러가_반환된다() throws Exception {
        // given
        mockMvc.perform(post("/owner/shops")
                .header("Authorization", "Bearer " + token_현수)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                        "address": "대전광역시 유성구 대학로 291",
                        "category_ids": [
                            %d
                        ],
                        "delivery": true,
                        "delivery_price": 4000,
                        "description": "테스트 상점2입니다.",
                        "image_urls": [
                            "",
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
                    """, shopCategory_치킨.getId())
                ))
            .andExpect(status().isBadRequest());
    }
}
