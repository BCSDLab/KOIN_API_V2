package in.koreatech.koin.admin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.shop.repository.AdminMenuCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminMenuRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.MenuImage;
import in.koreatech.koin.domain.shop.model.MenuOption;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.MenuCategoryFixture;
import in.koreatech.koin.fixture.MenuFixture;
import in.koreatech.koin.fixture.ShopCategoryFixture;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminShopApiTest extends AcceptanceTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AdminShopCategoryRepository adminShopCategoryRepository;

    @Autowired
    private AdminShopRepository adminShopRepository;

    @Autowired
    private AdminMenuRepository adminMenuRepository;

    @Autowired
    private AdminMenuCategoryRepository adminMenuCategoryRepository;

    @Autowired
    private MenuFixture menuFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ShopFixture shopFixture;

    @Autowired
    private ShopCategoryFixture shopCategoryFixture;

    @Autowired
    private MenuCategoryFixture menuCategoryFixture;

    private Owner owner_현수;
    private Owner owner_준영;
    private Shop shop_마슬랜;
    private User admin;
    private String token_admin;
    private ShopCategory shopCategory_치킨;
    private ShopCategory shopCategory_일반;
    private MenuCategory menuCategory_메인;
    private MenuCategory menuCategory_사이드;

    @BeforeAll
    void setUp() {
        admin = userFixture.코인_운영자();
        token_admin = userFixture.getToken(admin);
        owner_현수 = userFixture.현수_사장님();
        owner_준영 = userFixture.준영_사장님();
        shop_마슬랜 = shopFixture.마슬랜(owner_현수);
        shopCategory_치킨 = shopCategoryFixture.카테고리_치킨();
        shopCategory_일반 = shopCategoryFixture.카테고리_일반음식();
        menuCategory_메인 = menuCategoryFixture.메인메뉴(shop_마슬랜);
        menuCategory_사이드 = menuCategoryFixture.사이드메뉴(shop_마슬랜);
    }

    @Test
    @DisplayName("어드민이 모든 상점을 조회한다.")
    void findAllShops() {
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

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .when()
            .param("page", 1)
            .param("is_deleted", false)
            .get("/admin/shops")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getInt("total_count")).isEqualTo(13);
                softly.assertThat(response.body().jsonPath().getInt("current_count")).isEqualTo(10);
                softly.assertThat(response.body().jsonPath().getInt("total_page")).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getInt("current_page")).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getList("shops").size()).isEqualTo(10);
            }
        );
    }

    @Test
    @DisplayName("어드민이 특정 상점을 조회한다.")
    void findShop() {
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .when()
            .get("/admin/shops/{shopId}", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                     "updated_at": "2024-01-15",
                     "is_deleted": false,
                     "is_event": false,
                     "bank": "국민",
                     "account_number": "01022595923"
                 }
                """);
    }

    @Test
    @DisplayName("어드민이 상점의 모든 카테고리를 조회한다.")
    void findShopCategories() {
        for (int i = 0; i < 12; i++) {
            ShopCategory request = ShopCategory.builder()
                .name("카테고리" + i)
                .isDeleted(false)
                .build();
            adminShopCategoryRepository.save(request);
            System.out.println(i);
        }

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .when()
            .param("page", 1)
            .param("is_deleted", false)
            .get("/admin/shops/categories")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getInt("total_count")).isEqualTo(14);
                softly.assertThat(response.body().jsonPath().getInt("current_count")).isEqualTo(10);
                softly.assertThat(response.body().jsonPath().getInt("total_page")).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getInt("current_page")).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getList("categories").size()).isEqualTo(10);
            }
        );
    }

    @Test
    @DisplayName("어드민이 상점의 특정 카테고리를 조회한다.")
    void findShopCategory() {
        ShopCategory shopCategory = shopCategoryFixture.카테고리_치킨();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("id", shopCategory.getId())
            .when()
            .get("/admin/shops/categories/{id}")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                  "id": 3,
                  "image_url": "https://test-image.com/ckicken.jpg",
                  "name": "치킨"
                }
                """);
    }

    @Test
    @DisplayName("어드민이 특정 상점의 모든 메뉴를 조회한다.")
    void findShopMenus() {
        // given
        menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("id", shop_마슬랜.getId())
            .when()
            .get("/admin/shops/{id}/menus")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                """);
    }

    @Test
    @DisplayName("어드민이 특정 상점의 메뉴 카테고리들을 조회한다.")
    void findShopMenuCategories() {
        // given
        menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);
        var response = RestAssured
            .given()
            .pathParam("id", shop_마슬랜.getId())
            .header("Authorization", "Bearer " + token_admin)
            .when()
            .get("/admin/shops/{id}/menus/categories")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();
        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                """);
    }

    @Test
    @DisplayName("어드민이 특정 상점의 특정 메뉴를 조회한다.")
    void findShopMenu() {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("shopId", shop_마슬랜.getId())
            .pathParam("menuId", menu.getId())
            .when()
            .get("/admin/shops/{shopId}/menus/{menuId}", menu.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();
        System.out.println(JsonAssertions.assertThat(response.asPrettyString()));
        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                """);
    }

    @Test
    @DisplayName("어드민이 상점을 생성한다.")
    void createShop() {
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                    "address": "대전광역시 유성구 대학로 291",
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
            .when()
            .post("/admin/shops")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            Shop result = adminShopRepository.getById(2);
            assertSoftly(
                softly -> {
                    softly.assertThat(result.getAddress()).isEqualTo("대전광역시 유성구 대학로 291");
                    softly.assertThat(result.getDeliveryPrice()).isEqualTo(4000);
                    softly.assertThat(result.getDescription()).isEqualTo("테스트 상점2입니다.");
                    softly.assertThat(result.getName()).isEqualTo("테스트 상점2");
                    softly.assertThat(result.getShopImages()).hasSize(3);
                    softly.assertThat(result.getShopOpens()).hasSize(7);
                    softly.assertThat(result.getShopCategories()).hasSize(1);
                }
            );
        });
    }

    @Test
    @DisplayName("어드민이 상점 카테고리를 생성한다.")
    void createShopCategory() {
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "image_url": "https://image.png",
                  "name": "새로운 카테고리"
                }
                """)
            .when()
            .post("/admin/shops/categories")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            ShopCategory result = adminShopCategoryRepository.getById(3);
            assertSoftly(
                softly -> {
                    softly.assertThat(result.getImageUrl()).isEqualTo("https://image.png");
                    softly.assertThat(result.getName()).isEqualTo("새로운 카테고리");
                    softly.assertThat(result.isDeleted()).isEqualTo(false);
                }
            );
        });
    }

    @Test
    @DisplayName("어드민이 옵션이 여러개인 메뉴를 추가한다.")
    void createManyOptionMenu() {
        // given
        MenuCategory menuCategory = menuCategory_메인;
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .contentType(ContentType.JSON)
            .body(String.format("""
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
            .when()
            .post("/admin/shops/{id}/menus", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
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
        });
    }

    @Test
    @DisplayName("어드민이 옵션이 한개인 메뉴를 추가한다.")
    void createOneOptionMenu() {
        // given
        MenuCategory menuCategory = menuCategory_메인;
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .contentType(ContentType.JSON)
            .body(String.format("""
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
            .when()
            .post("/admin/shops/{id}/menus", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
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

                    softly.assertThat(menuOptions.get(0).getPrice()).isEqualTo(10000);
                }
            );
        });
    }

    @Test
    @DisplayName("어드민이 메뉴 카테고리를 추가한다.")
    void createMenuCategory() {
        // given
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("id", shop_마슬랜.getId())
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                   "name": "대박메뉴"
                }
                """))
            .when()
            .post("/admin/shops/{id}/menus/categories")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        var menuCategories = adminMenuCategoryRepository.findAllByShopId(shop_마슬랜.getId());

        assertThat(menuCategories).anyMatch(menuCategory -> "대박메뉴".equals(menuCategory.getName()));
    }

    @Test
    @DisplayName("어드민이 상점 삭제를 해제한다.")
    void cancelShopDeleted() {
        // given
        System.out.println("qwe");
        adminShopRepository.deleteById(shop_마슬랜.getId());
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("id", shop_마슬랜.getId())
            .contentType(ContentType.JSON)
            .when()
            .post("/admin/shops/{id}/undelete")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();
        var shop = adminShopRepository.getById(shop_마슬랜.getId());
        assertSoftly(softly -> softly.assertThat(shop.isDeleted()).isFalse());
    }

    @Test
    @DisplayName("어드민이 상점을 수정한다.")
    void modifyShop() {
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .contentType(ContentType.JSON)
            .body(String.format("""
            {
              "address": "충청남도 천안시 동남구 병천면 충절로 1600",
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
            """, shopCategory_일반.getId(), shopCategory_치킨.getId()
            ))
            .when()
            .put("/admin/shops/{id}", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            Shop result = adminShopRepository.getById(shop_마슬랜.getId());
            List<ShopImage> shopImages = result.getShopImages();
            List<ShopOpen> shopOpens = result.getShopOpens();
            Set<ShopCategoryMap> shopCategoryMaps = result.getShopCategories();

            assertSoftly(softly -> {
                softly.assertThat(result.getAddress()).isEqualTo("충청남도 천안시 동남구 병천면 충절로 1600");
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
    @DisplayName("어드민이 상점 카테고리를 수정한다.")
    void modifyShopCategory() {
        ShopCategory shopCategory = shopCategoryFixture.카테고리_일반음식();

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .contentType(ContentType.JSON)
            .pathParam("id", shopCategory.getId())
            .body("""
                {
                  "image_url": "http://image.png",
                  "name": "수정된 카테고리 이름"
                }
                """)
            .when()
            .put("/admin/shops/categories/{id}")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            ShopCategory updatedCategory = adminShopCategoryRepository.getById(shopCategory.getId());
            assertSoftly(
                softly -> {
                    softly.assertThat(updatedCategory.getId()).isEqualTo(shopCategory.getId());
                    softly.assertThat(updatedCategory.getImageUrl()).isEqualTo("http://image.png");
                    softly.assertThat(updatedCategory.getName()).isEqualTo("수정된 카테고리 이름");
                }
            );
        });
    }

    @Test
    @DisplayName("어드민이 특점 상점의 메뉴 카테고리를 수정한다.")
    void modifyMenuCategory() {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .contentType(ContentType.JSON)
            .pathParam("shopId", shop_마슬랜.getId())
            .body(String.format("""
                {
                   "id": %s,
                   "name": "사이드 메뉴"
                }
                """, menuCategory_메인.getId()))
            .when()
            .put("/admin/shops/{shopId}/menus/categories")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        MenuCategory menuCategory = adminMenuCategoryRepository.getById(menuCategory_메인.getId());
        assertSoftly(softly -> softly.assertThat(menuCategory.getName()).isEqualTo("사이드 메뉴"));
    }

    @Test
    @DisplayName("어드민이 특정 삼점의 메뉴를 단일 메뉴로 수정한다.")
    void modifyOneMenu() {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .contentType(ContentType.JSON)
            .pathParam("shopId", shop_마슬랜.getId())
            .pathParam("menuId", menu.getId())
            .body(String.format("""
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
            .when()
            .put("/admin/shops/{shopId}/menus/{menuId}")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

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
    @DisplayName("어드민이 특정 상점의 메뉴를 여러옵션을 가진 메뉴로 수정한다.")
    void modifyManyOptionMenu() {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("shopId", shop_마슬랜.getId())
            .pathParam("menuId", menu.getId())
            .contentType(ContentType.JSON)
            .body(String.format("""
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
            .when()
            .put("/admin/shops/{shopId}/menus/{menuId}")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

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
    @DisplayName("어드민이 특정 상점의 메뉴를 여러옵션을 가진 메뉴로 수정한다. - 가격 옵션이 비어있거나 null이면 400 에러 발생")
    void modifyManyOptionMenuWithEmptyOptionPrices() {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("shopId", shop_마슬랜.getId())
            .pathParam("menuId", menu.getId())
            .contentType(ContentType.JSON)
            .body(String.format("""
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
            .when()
            .put("/admin/shops/{shopId}/menus/{menuId}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }


    @Test
    @DisplayName("어드민이 상점을 삭제한다.")
    void deleteShop() {
        Shop shop = shopFixture.영업중이_아닌_신전_떡볶이(owner_현수);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .when()
            .delete("/admin/shops/{id}", shop.getId())
            .then()
            .statusCode(HttpStatus.OK.value());

        Shop deletedShop = adminShopRepository.getById(shop.getId());
        assertSoftly(softly -> softly.assertThat(deletedShop.isDeleted()).isTrue());
    }

    @Test
    @DisplayName("어드민이 상점 카테고리를 삭제한다.")
    void deleteShopCategory() {
        ShopCategory shopCategory = shopCategoryFixture.카테고리_일반음식();

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .when()
            .delete("/admin/shops/categories/{id}", shopCategory.getId())
            .then()
            .statusCode(HttpStatus.OK.value());

        ShopCategory deletedCategory = adminShopCategoryRepository.getById(shopCategory.getId());
        assertSoftly(softly -> softly.assertThat(deletedCategory.isDeleted()).isTrue());
    }

    @Test
    @DisplayName("어드민이 특정 상점의 메뉴 카테고리를 삭제한다.")
    void deleteMenuCategory() {
        // when & then
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("shopId", shop_마슬랜.getId())
            .pathParam("categoryId", menuCategory_메인.getId())
            .when()
            .delete("/admin/shops/{shopId}/menus/categories/{categoryId}")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        assertThat(adminMenuCategoryRepository.findById(menuCategory_메인.getId())).isNotPresent();
    }

    @Test
    @DisplayName("어드민이 메뉴를 삭제한다.")
    void deleteMenu() {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("shopId", shop_마슬랜.getId())
            .pathParam("menuId", menu.getId())
            .when()
            .delete("/admin/shops/{shopId}/menus/{menuId}", menu.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        assertThat(adminMenuRepository.findById(menu.getId())).isNotPresent();
    }
}
