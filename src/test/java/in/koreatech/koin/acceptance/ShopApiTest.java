package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
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
import in.koreatech.koin.domain.shop.repository.MenuCategoryRepository;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryMapRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopOpenRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class ShopApiTest extends AcceptanceTest {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private ShopImageRepository shopImageRepository;

    @Autowired
    private ShopOpenRepository shopOpenRepository;

    @Autowired
    private ShopCategoryMapRepository shopCategoryMapRepository;

    @Autowired
    private ShopCategoryRepository shopCategoryRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private ShopRepository shopRepository;

    private ShopCategory shopCategory1, shopCategory2;
    private Shop shop;
    private Owner owner;

    @BeforeEach
    void setUp() {

        Owner ownerRequest = Owner.builder()
            .companyRegistrationNumber("123-45-67890")
            .companyRegistrationCertificateImageUrl("https://test.com/test.jpg")
            .grantShop(true)
            .grantEvent(true)
            .user(
                User.builder()
                    .password("1234")
                    .nickname("주노")
                    .name("최준호")
                    .phoneNumber("010-1234-5678")
                    .userType(OWNER)
                    .gender(UserGender.MAN)
                    .email("test@koreatech.ac.kr")
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();
        owner = ownerRepository.save(ownerRequest);

        Shop shopRequest = Shop.builder()
            .owner(owner)
            .name("테스트 상점")
            .internalName("테스트")
            .chosung("테스트")
            .phone("010-1234-5678")
            .address("대전광역시 유성구 대학로 291")
            .description("테스트 상점입니다.")
            .delivery(true)
            .deliveryPrice(3000L)
            .payCard(true)
            .payBank(true)
            .isDeleted(false)
            .isEvent(false)
            .remarks("비고")
            .hit(0L)
            .build();
        shop = shopRepository.save(shopRequest);

        ShopCategory shopCategoryRequest1 = ShopCategory.builder()
            .isDeleted(false)
            .name("테스트1")
            .imageUrl("https://test.com/test1.jpg")
            .build();

        ShopCategory shopCategoryRequest2 = ShopCategory.builder()
            .isDeleted(false)
            .name("테스트2")
            .imageUrl("https://test.com/test2.jpg")
            .build();
        shopCategory1 = shopCategoryRepository.save(shopCategoryRequest1);
        shopCategory2 = shopCategoryRepository.save(shopCategoryRequest2);
    }

    @Test
    @DisplayName("옵션이 하나 있는 상점의 메뉴를 조회한다.")
    void findMenuSingleOption() {
        // given
        MenuOption menuOption = MenuOption.builder()
            .option("일반")
            .price(7000)
            .build();

        MenuImage menuImage = MenuImage.builder()
            .imageUrl("https://test.com/test.jpg")
            .build();

        Menu menu = Menu.builder()
            .shopId(1L)
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();

        MenuCategory menuCategory = MenuCategory.builder()
            .shopId(1L)
            .name("중식")
            .build();
        menuCategoryRepository.save(menuCategory);

        MenuCategoryMap menuCategoryMap = MenuCategoryMap.create();

        menuOption.setMenu(menu);
        menuImage.setMenu(menu);

        // when then
        menuCategoryMap.map(menu, menuCategory);
        menuRepository.save(menu);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/menus/{menuId}", menu.getShopId(), menu.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("id")).isEqualTo(menu.getId());

                softly.assertThat(response.body().jsonPath().getLong("shop_id")).isEqualTo(menu.getShopId());
                softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo(menu.getName());
                softly.assertThat(response.body().jsonPath().getBoolean("is_hidden")).isEqualTo(menu.getIsHidden());

                softly.assertThat(response.body().jsonPath().getBoolean("is_single")).isTrue();
                softly.assertThat(response.body().jsonPath().getInt("single_price")).isEqualTo(menuOption.getPrice());

                softly.assertThat(response.body().jsonPath().getList("option_prices")).isNull();

                softly.assertThat(response.body().jsonPath().getString("description")).isEqualTo(menu.getDescription());

                softly.assertThat(response.body().jsonPath().getList("category_ids"))
                    .hasSize(menu.getMenuCategoryMaps().size());

                softly.assertThat(response.body().jsonPath().getList("image_urls"))
                    .hasSize(menu.getMenuImages().size());
            }
        );
    }

    @Test
    @DisplayName("옵션이 여러 개 있는 상점의 메뉴를 조회한다.")
    void findMenuMultipleOption() {
        // given
        MenuOption menuOption1 = MenuOption.builder()
            .option("일반")
            .price(7000)
            .build();

        MenuOption menuOption2 = MenuOption.builder()
            .option("곱빼기")
            .price(7500)
            .build();

        MenuImage menuImage1 = MenuImage.builder()
            .imageUrl("https://test.com/test.jpg")
            .build();
        MenuImage menuImage2 = MenuImage.builder()
            .imageUrl("https://test.com/hello.jpg")
            .build();

        Menu menu = Menu.builder()
            .shopId(1L)
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();

        MenuCategory menuCategory = MenuCategory.builder()
            .shopId(1L)
            .name("중식")
            .build();

        MenuCategoryMap menuCategoryMap = MenuCategoryMap.create();
        menuCategoryRepository.save(menuCategory);

        // when then
        menuOption1.setMenu(menu);
        menuOption2.setMenu(menu);
        menuImage1.setMenu(menu);
        menuImage2.setMenu(menu);

        menuCategoryMap.map(menu, menuCategory);

        menuRepository.save(menu);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/menus/{menuId}", menu.getShopId(), menu.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("id")).isEqualTo(menu.getId());

                softly.assertThat(response.body().jsonPath().getLong("shop_id")).isEqualTo(menu.getShopId());
                softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo(menu.getName());
                softly.assertThat(response.body().jsonPath().getBoolean("is_hidden")).isEqualTo(menu.getIsHidden());

                softly.assertThat(response.body().jsonPath().getBoolean("is_single")).isFalse();
                softly.assertThat((Integer) response.body().jsonPath().get("single_price")).isNull();

                softly.assertThat(response.body().jsonPath().getList("option_prices")).hasSize(2);
                softly.assertThat(response.body().jsonPath().getString("option_prices[0].option"))
                    .isEqualTo(menu.getMenuOptions().get(0).getOption());
                softly.assertThat(response.body().jsonPath().getInt("option_prices[0].price"))
                    .isEqualTo(menu.getMenuOptions().get(0).getPrice());
                softly.assertThat(response.body().jsonPath().getString("option_prices[1].option"))
                    .isEqualTo(menu.getMenuOptions().get(1).getOption());
                softly.assertThat(response.body().jsonPath().getInt("option_prices[1].price"))
                    .isEqualTo(menu.getMenuOptions().get(1).getPrice());

                softly.assertThat(response.body().jsonPath().getString("description")).isEqualTo(menu.getDescription());

                softly.assertThat(response.body().jsonPath().getList("category_ids"))
                    .hasSize(menu.getMenuCategoryMaps().size());

                softly.assertThat(response.body().jsonPath().getList("image_urls"))
                    .hasSize(menu.getMenuImages().size());
            }
        );
    }

    @Test
    @DisplayName("상점의 메뉴 카테고리들을 조회한다.")
    void findShopMenuCategories() {
        // given
        final long SHOP_ID = 1L;

        Menu menu = Menu.builder()
            .shopId(SHOP_ID)
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();

        MenuCategory menuCategory1 = MenuCategory.builder()
            .shopId(SHOP_ID)
            .name("이벤트 메뉴")
            .build();

        MenuCategory menuCategory2 = MenuCategory.builder()
            .shopId(SHOP_ID)
            .name("메인 메뉴")
            .build();

        menuCategoryRepository.save(menuCategory1);
        menuCategoryRepository.save(menuCategory2);

        MenuCategoryMap menuCategoryMap1 = MenuCategoryMap.create();
        MenuCategoryMap menuCategoryMap2 = MenuCategoryMap.create();

        // when then
        menuCategoryMap1.map(menu, menuCategory1);
        menuCategoryMap2.map(menu, menuCategory2);

        menuRepository.save(menu);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/menus/categories", menu.getShopId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("count")).isEqualTo(2);

                softly.assertThat(response.body().jsonPath().getList("menu_categories"))
                    .hasSize(2);

                softly.assertThat(response.body().jsonPath().getLong("menu_categories[0].id"))
                    .isEqualTo(menuCategory1.getId());
                softly.assertThat(response.body().jsonPath().getString("menu_categories[0].name"))
                    .isEqualTo(menuCategory1.getName());

                softly.assertThat(response.body().jsonPath().getLong("menu_categories[1].id"))
                    .isEqualTo(menuCategory2.getId());
                softly.assertThat(response.body().jsonPath().getString("menu_categories[1].name"))
                    .isEqualTo(menuCategory2.getName());
            }
        );
    }

    @Test
    @DisplayName("특정 상점 조회")
    void getShop() {
        // given
        ShopOpen open1 = ShopOpen.builder()
            .openTime(LocalTime.of(9, 0))
            .closeTime(LocalTime.of(21, 0))
            .shop(shop)
            .closed(false)
            .dayOfWeek("MONDAY")
            .build();

        ShopOpen open2 = ShopOpen.builder()
            .openTime(LocalTime.of(10, 0))
            .closeTime(LocalTime.of(20, 30))
            .shop(shop)
            .closed(false)
            .dayOfWeek("FRIDAY")
            .build();

        ShopOpen newShopOpen1 = shopOpenRepository.save(open1);
        ShopOpen newShopOpen2 = shopOpenRepository.save(open2);

        ShopCategoryMap shopCategoryMap1 = ShopCategoryMap.builder()
            .shop(shop)
            .shopCategory(shopCategory1)
            .build();

        ShopCategoryMap shopCategoryMap2 = ShopCategoryMap.builder()
            .shop(shop)
            .shopCategory(shopCategory2)
            .build();

        ShopCategoryMap newShopCategoryMap1 = shopCategoryMapRepository.save(shopCategoryMap1);
        ShopCategoryMap newShopCategoryMap2 = shopCategoryMapRepository.save(shopCategoryMap2);

        ShopImage shopImage1 = ShopImage.builder()
            .imageUrl("https://test.com/test1.jpg")
            .shop(shop)
            .build();

        ShopImage shopImage2 = ShopImage.builder()
            .imageUrl("https://test.com/test2.jpg")
            .shop(shop)
            .build();

        ShopImage newShopImage1 = shopImageRepository.save(shopImage1);
        ShopImage newShopImage2 = shopImageRepository.save(shopImage2);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops/1")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        List<ShopImage> savedShopImages = shopImageRepository.findAllByShopId(shop.getId());
        List<MenuCategory> savedMenuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        List<ShopOpen> savedShopOpens = shopOpenRepository.findAllByShopId(shop.getId());
        List<ShopCategoryMap> savedShopCategoryMaps = shopCategoryMapRepository.findAllByShopId(shop.getId());

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("address")).isEqualTo(shop.getAddress());
                softly.assertThat(response.body().jsonPath().getBoolean("delivery")).isEqualTo(shop.getDelivery());
                softly.assertThat(response.body().jsonPath().getLong("delivery_price"))
                    .isEqualTo(shop.getDeliveryPrice());
                softly.assertThat(response.body().jsonPath().getString("description")).isEqualTo(shop.getDescription());
                softly.assertThat(response.body().jsonPath().getLong("id")).isEqualTo(shop.getId());
                softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo(shop.getName());
                softly.assertThat(response.body().jsonPath().getBoolean("pay_bank")).isEqualTo(shop.getPayBank());
                softly.assertThat(response.body().jsonPath().getBoolean("pay_card")).isEqualTo(shop.getPayCard());
                softly.assertThat(response.body().jsonPath().getString("phone")).isEqualTo(shop.getPhone());

                softly.assertThat(response.body().jsonPath().getList("image_urls")).hasSize(savedShopImages.size());
                softly.assertThat(response.body().jsonPath().getList("menu_categories"))
                    .hasSize(savedMenuCategories.size());
                softly.assertThat(response.body().jsonPath().getList("open")).hasSize(savedShopOpens.size());
                softly.assertThat(response.body().jsonPath().getList("shop_categories"))
                    .hasSize(savedShopCategoryMaps.size());
            }
        );
    }
}
