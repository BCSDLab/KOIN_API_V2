package in.koreatech.koin.acceptance;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.EventArticle;
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
import in.koreatech.koin.domain.shop.repository.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.fixture.EventArticleFixture;
import in.koreatech.koin.fixture.MenuCategoryFixture;
import in.koreatech.koin.fixture.MenuFixture;
import in.koreatech.koin.fixture.ShopCategoryFixture;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
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
    private MenuFixture menuFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ShopFixture shopFixture;

    @Autowired
    private ShopCategoryFixture shopCategoryFixture;

    @Autowired
    private MenuCategoryFixture menuCategoryFixture;

    @Autowired
    private EventArticleFixture eventArticleFixture;

    private Owner owner_현수;
    private String token_현수;
    private Owner owner_준영;
    private String token_준영;
    private Shop shop_마슬랜;
    private ShopCategory shopCategory_치킨;
    private ShopCategory shopCategory_일반;
    private MenuCategory menuCategory_메인;
    private MenuCategory menuCategory_사이드;

    @BeforeEach
    void setUp() {
        owner_현수 = userFixture.현수_사장님();
        token_현수 = userFixture.getToken(owner_현수.getUser());
        owner_준영 = userFixture.준영_사장님();
        token_준영 = userFixture.getToken(owner_준영.getUser());
        shop_마슬랜 = shopFixture.마슬랜(owner_현수);
        shopCategory_치킨 = shopCategoryFixture.카테고리_치킨();
        shopCategory_일반 = shopCategoryFixture.카테고리_일반음식();
        menuCategory_메인 = menuCategoryFixture.메인메뉴(shop_마슬랜);
        menuCategory_사이드 = menuCategoryFixture.사이드메뉴(shop_마슬랜);
    }

    @Test
    @DisplayName("사장님의 가게 목록을 조회한다.")
    void getOwnerShops() {
        // given
        shopFixture.신전_떡볶이(owner_현수);

        // when then
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .when()
            .get("/owner/shops")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                """);
    }

    @Test
    @DisplayName("상점을 생성한다.")
    void createOwnerShop() {
        // given
        RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_현수)
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
            .post("/owner/shops")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
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
                }
            );
        });
    }

    @Test
    @DisplayName("상점 사장님이 특정 상점 조회")
    void getShop() {
        // given
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .when()
            .get("/owner/shops/{shopId}", shop_마슬랜.getId())
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
                     "is_event": false
                 }
                """);
    }

    @Test
    @DisplayName("특정 상점의 모든 메뉴를 조회한다.")
    void findOwnerShopMenu() {
        // given
        menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .param("shopId", shop_마슬랜.getId())
            .when()
            .get("/owner/shops/menus")
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
    @DisplayName("사장님이 자신의 상점 메뉴 카테고리들을 조회한다.")
    void findOwnerMenuCategories() {
        // given
        menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        var response = RestAssured
            .given()
            .param("shopId", shop_마슬랜.getId())
            .header("Authorization", "Bearer " + token_현수)
            .when()
            .get("/owner/shops/menus/categories")
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
    @DisplayName("사장님이 자신의 상점의 특정 메뉴를 조회한다.")
    void findMenuShopOwner() {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .when()
            .get("/owner/shops/menus/{menuId}", menu.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(menu.getId());

                softly.assertThat(response.body().jsonPath().getInt("shop_id")).isEqualTo(menu.getShopId());
                softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo(menu.getName());
                softly.assertThat(response.body().jsonPath().getBoolean("is_hidden")).isEqualTo(menu.isHidden());

                softly.assertThat(response.body().jsonPath().getBoolean("is_single")).isFalse();
                softly.assertThat((Integer)response.body().jsonPath().get("single_price")).isNull();
                softly.assertThat(response.body().jsonPath().getList("option_prices")).hasSize(2);
                softly.assertThat(response.body().jsonPath().getString("description")).isEqualTo(menu.getDescription());
                softly.assertThat(response.body().jsonPath().getList("category_ids"))
                    .hasSize(menu.getMenuCategoryMaps().size());
                softly.assertThat(response.body().jsonPath().getList("image_urls"))
                    .hasSize(menu.getMenuImages().size());
            }
        );
    }

    @Test
    @DisplayName("권한이 없는 상점 사장님이 특정 상점 조회")
    void ownerCannotQueryOtherStoresWithoutPermission() {
        // given
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_준영)
            .when()
            .get("/owner/shops/{shopId}", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("사장님이 메뉴 카테고리를 삭제한다.")
    void deleteMenuCategory() {
        // when & then
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .when()
            .delete("/owner/shops/menus/categories/{categoryId}", menuCategory_메인.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        assertThat(menuCategoryRepository.findById(menuCategory_메인.getId())).isNotPresent();
    }

    @Test
    @DisplayName("사장님이 메뉴를 삭제한다.")
    void deleteMenu() {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .when()
            .delete("/owner/shops/menus/{menuId}", menu.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        assertThat(menuRepository.findById(menu.getId())).isNotPresent();
    }

    @Test
    @DisplayName("사장님이 옵션이 여러개인 메뉴를 추가한다.")
    void createManyOptionMenu() {
        // given
        MenuCategory menuCategory = menuCategory_메인;
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
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
            .post("/owner/shops/{id}/menus", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
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
        });
    }

    @Test
    @DisplayName("사장님이 옵션이 한개인 메뉴를 추가한다.")
    void createOneOptionMenu() {
        // given
        MenuCategory menuCategory = menuCategory_메인;
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
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
            .post("/owner/shops/{id}/menus", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
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
    @DisplayName("사장님이 메뉴 카테고리를 추가한다.")
    void createMenuCategory() {
        // given
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                   "name": "대박메뉴"
                }
                """))
            .when()
            .post("/owner/shops/{id}/menus/categories", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        var menuCategories = menuCategoryRepository.findAllByShopId(shop_마슬랜.getId());

        assertThat(menuCategories).anyMatch(menuCategory -> "대박메뉴".equals(menuCategory.getName()));
    }

    @Test
    @DisplayName("사장님이 단일 메뉴로 수정한다.")
    void modifyOneMenu() {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .contentType(ContentType.JSON)
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
            .put("/owner/shops/menus/{menuId}", menu.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

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
    @DisplayName("사장님이 여러옵션을 가진 메뉴로 수정한다.")
    void modifyManyOptionMenu() {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
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
            .put("/owner/shops/menus/{menuId}", menu.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

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
    @DisplayName("사장님이 상점을 수정한다.")
    void modifyShop() {
        // given
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
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
            ))
            .when()
            .put("/owner/shops/{id}", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            Shop result = shopRepository.getById(1);
            List<ShopImage> shopImages = result.getShopImages();
            List<ShopOpen> shopOpens = result.getShopOpens();
            List<ShopCategoryMap> shopCategoryMaps = result.getShopCategories();
            assertSoftly(
                softly -> {
                    softly.assertThat(result.getAddress()).isEqualTo("충청남도 천안시 동남구 병천면 충절로 1600");
                    softly.assertThat(result.isDeleted()).isFalse();
                    softly.assertThat(result.getDeliveryPrice()).isEqualTo(1000);
                    softly.assertThat(result.getDescription()).isEqualTo("이번주 전 메뉴 10% 할인 이벤트합니다.");
                    softly.assertThat(result.getName()).isEqualTo("써니 숯불 도시락");
                    softly.assertThat(result.isPayBank()).isTrue();
                    softly.assertThat(result.isPayCard()).isTrue();
                    softly.assertThat(result.getPhone()).isEqualTo("041-123-4567");

                    softly.assertThat(shopCategoryMaps.get(0).getShopCategory().getId()).isEqualTo(1);
                    softly.assertThat(shopCategoryMaps.get(1).getShopCategory().getId()).isEqualTo(2);

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
    @DisplayName("권한이 없는 상점 사장님이 특정 카테고리 조회한다.")
    void ownerCannotQueryOtherCategoriesWithoutPermission() {
        // given
        RestAssured
            .given()
            .param("shopId", 1)
            .header("Authorization", "Bearer " + token_준영)
            .when()
            .get("/owner/shops/menus/categories")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("권한이 없는 상점 사장님이 특정 메뉴 조회한다.")
    void ownerCannotQueryOtherMenusWithoutPermission() {
        // given
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_준영)
            .param("shopId", 1)
            .when()
            .get("/owner/shops/menus")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("권한이 없는 사장님이 메뉴 카테고리를 삭제한다.")
    void ownerCannotDeleteOtherCategoriesWithoutPermission() {
        // given
        MenuCategory menuCategory = menuCategoryFixture.세트메뉴(shop_마슬랜);
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_준영)
            .when()
            .delete("/owner/shops/menus/categories/{categoryId}", menuCategory.getId())
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("권한이 없는 사장님이 메뉴를 삭제한다.")
    void ownerCannotDeleteOtherMenusWithoutPermission() {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop_마슬랜, menuCategory_메인);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_준영)
            .when()
            .delete("/owner/shops/menus/{menuId}", menu.getId())
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("사장님이 이벤트를 추가한다.")
    void ownerShopCreateEvent() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(10);
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .contentType(ContentType.JSON)
            .body(String.format("""
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
            ))
            .when()
            .post("/owner/shops/{shopId}/event", shop_마슬랜.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
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
        });
        verify(shopEventListener).onShopEventCreate(any());
    }

    @Test
    @DisplayName("사장님이 이벤트를 수정한다.")
    void ownerShopModifyEvent() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(10);
        EventArticle eventArticle = eventArticleFixture.할인_이벤트(shop_마슬랜, startDate, endDate);
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .contentType(ContentType.JSON)
            .body(String.format("""
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
                endDate))
            .when()
            .put("/owner/shops/{shopId}/events/{eventId}", shop_마슬랜.getId(), eventArticle.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
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
        });
    }

    @Test
    @DisplayName("사장님이 이벤트를 삭제한다.")
    void ownerShopDeleteEvent() {
        EventArticle eventArticle = eventArticleFixture.할인_이벤트(
            shop_마슬랜,
            LocalDate.of(2024, 10, 24),
            LocalDate.of(2024, 10, 26)
        );

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_현수)
            .contentType(ContentType.JSON)
            .when()
            .delete("/owner/shops/{shopId}/events/{eventId}", shop_마슬랜.getId(), eventArticle.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        Optional<EventArticle> modifiedEventArticle = eventArticleRepository.findById(eventArticle.getId());
        assertThat(modifiedEventArticle).isNotPresent();
    }
}
