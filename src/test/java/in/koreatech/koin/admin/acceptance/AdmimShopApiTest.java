package in.koreatech.koin.admin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.shop.repository.AdminMenuCategoryRepository;
import in.koreatech.koin.admin.shop.repository.AdminMenuRepository;
import in.koreatech.koin.admin.shop.repository.AdminShopRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.MenuImage;
import in.koreatech.koin.domain.shop.model.MenuOption;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.repository.EventArticleRepository;
import in.koreatech.koin.domain.user.model.User;
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
public class AdmimShopApiTest extends AcceptanceTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AdminMenuRepository menuRepository;

    @Autowired
    private AdminShopRepository shopRepository;

    @Autowired
    private AdminMenuCategoryRepository menuCategoryRepository;

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
    private User admin;
    private String token_admin;
    private ShopCategory shopCategory_치킨;
    private ShopCategory shopCategory_일반;
    private MenuCategory menuCategory_메인;
    private MenuCategory menuCategory_사이드;

    @BeforeEach
    void setUp() {
        admin = userFixture.코인_운영자();
        token_admin = userFixture.getToken(admin);
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

        var menuCategories = menuCategoryRepository.findAllByShopId(shop_마슬랜.getId());

        assertThat(menuCategories).anyMatch(menuCategory -> "대박메뉴".equals(menuCategory.getName()));
    }

    @Test
    @DisplayName("어드민이 상점 삭제를 해제한다.")
    void cancelShopDeleted() {
        // given
        System.out.println("qwe");
        shopRepository.deleteById(shop_마슬랜.getId());
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
        var shop = shopRepository.getById(shop_마슬랜.getId());
        assertSoftly(softly -> softly.assertThat(shop.isDeleted()).isFalse());
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

        MenuCategory menuCategory = menuCategoryRepository.getById(menuCategory_메인.getId());
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

        assertThat(menuCategoryRepository.findById(menuCategory_메인.getId())).isNotPresent();
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

        assertThat(menuRepository.findById(menu.getId())).isNotPresent();
    }
}
