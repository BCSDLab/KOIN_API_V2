package in.koreatech.koin.admin.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.repository.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.EventArticleFixture;
import in.koreatech.koin.fixture.MenuCategoryFixture;
import in.koreatech.koin.fixture.MenuFixture;
import in.koreatech.koin.fixture.ShopCategoryFixture;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
public class AdmimShopApiTest extends AcceptanceTest {

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
    @DisplayName("특정 상점의 모든 메뉴를 조회한다.")
    void findOwnerShopMenu() {
        // given
        menuFixture.짜장면_옵션메뉴(shop_마슬랜, menuCategory_메인);
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("id", shop_마슬랜.getId())
            .when()
            .get("/admin/shops/{id}/menus")
            .then()
            .log().all()
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
}
