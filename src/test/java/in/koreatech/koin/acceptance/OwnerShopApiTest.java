package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.swing.text.html.Option;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.owner.repository.OwnerAttachmentRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.ownershop.exception.EventArticleNotFoundException;
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
import in.koreatech.koin.domain.shop.repository.ShopCategoryMapRepository;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopImageRepository;
import in.koreatech.koin.domain.shop.repository.ShopOpenRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class OwnerShopApiTest extends AcceptanceTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopCategoryRepository shopCategoryRepository;

    @Autowired
    private ShopOpenRepository shopOpenRepository;

    @Autowired
    private ShopImageRepository shopImageRepository;

    @Autowired
    private ShopCategoryMapRepository shopCategoryMapRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private EventArticleRepository eventArticleRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private OwnerAttachmentRepository ownerAttachmentRepository;

    private Owner owner;
    private Shop shop;
    private String token;
    private ShopCategory shopCategory1;
    private ShopCategory shopCategory2;
    private Owner otherOwner;
    private String otherOwnerToken;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(
            ZonedDateTime.parse("2024-02-21 18:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z")).toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        OwnerAttachment attachment = OwnerAttachment.builder()
            .url("https://test.com/test.jpg")
            .isDeleted(false)
            .build();

        Owner ownerRequest = Owner.builder()
            .companyRegistrationNumber("123-45-67890")
            .attachments(List.of(attachment))
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
        token = jwtProvider.createToken(owner.getUser());

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

        var otherAttachment = OwnerAttachment.builder()
            .url("https://test.com/test.jpg")
            .isDeleted(false)
            .build();

        Owner otherOwnerRequest = Owner.builder()
            .companyRegistrationNumber("123-45-67890")
            .attachments(List.of(otherAttachment))
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
        otherOwner = ownerRepository.save(otherOwnerRequest);
        otherOwnerToken = jwtProvider.createToken(otherOwner.getUser());
    }

    @Test
    @DisplayName("사장님의 가게 목록을 조회한다.")
    void getOwnerShops() {
        // given
        Shop shopRequest = Shop.builder()
            .owner(owner)
            .name("테스트 상점2")
            .internalName("테스트")
            .chosung("테스트")
            .phone("010-1234-5678")
            .address("대전광역시 유성구 대학로 291")
            .description("테스트 상점2입니다.")
            .delivery(true)
            .deliveryPrice(4000L)
            .payCard(true)
            .payBank(true)
            .isDeleted(false)
            .isEvent(false)
            .remarks("비고2")
            .hit(10L)
            .build();
        Shop shop2 = shopRepository.save(shopRequest);

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/owner/shops")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("count")).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getList("shops").size()).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getLong("shops[0].id")).isEqualTo(shop.getId());
                softly.assertThat(response.body().jsonPath().getString("shops[0].name")).isEqualTo(shop.getName());
                softly.assertThat(response.body().jsonPath().getLong("shops[1].id")).isEqualTo(shop2.getId());
                softly.assertThat(response.body().jsonPath().getString("shops[1].name")).isEqualTo(shop2.getName());
            }
        );
    }

    @Test
    @DisplayName("상점을 생성한다.")
    void createOwnerShop() {
        // given
        OwnerShopsRequest.InnerOpenRequest open1 = new OwnerShopsRequest.InnerOpenRequest(
            LocalTime.of(21, 0),
            false,
            "MONDAY",
            LocalTime.of(9, 0)
        );
        OwnerShopsRequest.InnerOpenRequest open2 = new OwnerShopsRequest.InnerOpenRequest(
            LocalTime.of(21, 0),
            false,
            "WEDNESDAY",
            LocalTime.of(9, 0)
        );

        List<Long> categoryIds = List.of(1L);
        List<String> imageUrls = List.of(
            "https://test.com/test1.jpg",
            "https://test.com/test2.jpg",
            "https://test.com/test3.jpg"
        );
        List<OwnerShopsRequest.InnerOpenRequest> opens = List.of(open1, open2);

        OwnerShopsRequest ownerShopsRequest = new OwnerShopsRequest(
            "대전광역시 유성구 대학로 291",
            categoryIds,
            true,
            4000L,
            "테스트 상점2입니다.",
            imageUrls,
            "테스트 상점2",
            opens,
            true,
            true,
            "010-1234-5678"
        );

        ShopCategory shopCategory1 = ShopCategory.builder()
            .isDeleted(false)
            .name("테스트1")
            .imageUrl("https://test.com/test1.jpg")
            .build();

        ShopCategory shopCategory2 = ShopCategory.builder()
            .isDeleted(false)
            .name("테스트2")
            .imageUrl("https://test.com/test2.jpg")
            .build();
        shopCategoryRepository.save(shopCategory1);
        shopCategoryRepository.save(shopCategory2);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(ownerShopsRequest)
            .when()
            .post("/owner/shops")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        List<Shop> shops = shopRepository.findAllByOwnerId(owner.getId());
        Shop createdShop = shops.get(1);

        List<ShopOpen> shopOpens = shopOpenRepository.findAllByShopId(createdShop.getId());
        List<ShopImage> shopImages = shopImageRepository.findAllByShopId(createdShop.getId());
        List<ShopCategoryMap> shopCategoryMaps = shopCategoryMapRepository.findAllByShopId(createdShop.getId());

        assertSoftly(
            softly -> {
                softly.assertThat(createdShop.getAddress()).isEqualTo(ownerShopsRequest.address());
                softly.assertThat(createdShop.getDelivery()).isEqualTo(ownerShopsRequest.delivery());
                softly.assertThat(createdShop.getDeliveryPrice()).isEqualTo(ownerShopsRequest.deliveryPrice());
                softly.assertThat(createdShop.getDescription()).isEqualTo(ownerShopsRequest.description());
                softly.assertThat(createdShop.getName()).isEqualTo(ownerShopsRequest.name());
                softly.assertThat(createdShop.getPayBank()).isEqualTo(ownerShopsRequest.payBank());
                softly.assertThat(createdShop.getPayCard()).isEqualTo(ownerShopsRequest.payCard());
                softly.assertThat(createdShop.getPhone()).isEqualTo(ownerShopsRequest.phone());
                softly.assertThat(shopOpens).hasSize(2);
                softly.assertThat(categoryIds).containsAnyElementsOf(shopCategoryMaps.stream()
                    .map(shopCategory -> shopCategory.getShopCategory().getId()).toList());
                softly.assertThat(imageUrls).containsAnyElementsOf(shopImages.stream()
                    .map(ShopImage::getImageUrl).toList());
            }
        );
    }

    @Test
    @DisplayName("상점 사장님이 특정 상점 조회")
    void getShop() {
        // given
        ShopOpen open1 = ShopOpen.builder()
            .openTime(LocalTime.of(0, 0))
            .closeTime(LocalTime.of(21, 0))
            .shop(shop)
            .closed(false)
            .dayOfWeek("MONDAY")
            .build();

        ShopOpen open2 = ShopOpen.builder()
            .openTime(LocalTime.of(0, 0))
            .closeTime(LocalTime.of(0, 0))
            .shop(shop)
            .closed(false)
            .dayOfWeek("FRIDAY")
            .build();

        shopOpenRepository.save(open1);
        shopOpenRepository.save(open2);

        ShopCategoryMap shopCategoryMap1 = ShopCategoryMap.builder()
            .shop(shop)
            .shopCategory(shopCategory1)
            .build();

        ShopCategoryMap shopCategoryMap2 = ShopCategoryMap.builder()
            .shop(shop)
            .shopCategory(shopCategory2)
            .build();

        shopCategoryMapRepository.save(shopCategoryMap1);
        shopCategoryMapRepository.save(shopCategoryMap2);

        ShopImage shopImage1 = ShopImage.builder()
            .imageUrl("https://test.com/test1.jpg")
            .shop(shop)
            .build();

        ShopImage shopImage2 = ShopImage.builder()
            .imageUrl("https://test.com/test2.jpg")
            .shop(shop)
            .build();

        shopImageRepository.save(shopImage1);
        shopImageRepository.save(shopImage2);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/owner/shops/{shopId}", shop.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        List<ShopImage> savedShopImages = shopImageRepository.findAllByShopId(shop.getId());
        List<MenuCategory> savedMenuCategories = menuCategoryRepository.findAllByShopId(shop.getId());
        List<ShopOpen> savedShopOpens = shopOpenRepository.findAllByShopId(shop.getId());
        List<ShopCategoryMap> savedShopCategoryMaps = shopCategoryMapRepository.findAllByShopId(shop.getId());

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                    "address": "대전광역시 유성구 대학로 291",
                    "delivery": true,
                    "delivery_price": 3000,
                    "description": "테스트 상점입니다.",
                    "id": 1,
                    "image_urls": [
                        "https://test.com/test1.jpg",
                        "https://test.com/test2.jpg"
                    ],
                    "menu_categories": [
                     
                    ],
                    "name": "테스트 상점",
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
                    "phone": "010-1234-5678",
                    "shop_categories": [
                        {
                            "id": 1,
                            "name": "테스트1"
                        },
                        {
                            "id": 2,
                            "name": "테스트2"
                        }
                    ],
                    "updated_at": "%s",
                    "is_event": false
                }
                """, LocalDateTime.now().format(ofPattern("yyyy-MM-dd"))));
    }

    @Test
    @DisplayName("특정 상점의 모든 메뉴를 조회한다.")
    void findOwnerShopMenu() {
        // given
        final long SHOP_ID = 1L;

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

        Menu menu1 = Menu.builder()
            .shopId(1L)
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();

        MenuCategory menuCategory1 = MenuCategory.builder()
            .shop(shop)
            .name("중식")
            .build();

        MenuCategoryMap menuCategoryMap1 = MenuCategoryMap.create();
        menuCategoryRepository.save(menuCategory1);

        menuOption1.setMenu(menu1);
        menuOption2.setMenu(menu1);
        menuImage1.setMenu(menu1);
        menuImage2.setMenu(menu1);

        menuCategoryMap1.map(menu1, menuCategory1);

        menuRepository.save(menu1);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .param("shopId", SHOP_ID)
            .when()
            .get("/owner/shops/menus")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("count")).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getLong("menu_categories[0].id"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenuCategory().getId());
                softly.assertThat(response.body().jsonPath().getString("menu_categories[0].name"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenuCategory().getName());
                softly.assertThat(response.body().jsonPath().getString("menu_categories[0].menus[0].description"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenu().getDescription());
                softly.assertThat(response.body().jsonPath().getLong("menu_categories[0].menus[0].id"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenu().getId());
                softly.assertThat(response.body().jsonPath().getList("menu_categories[0].menus[0].image_urls"))
                    .hasSize(2);
                softly.assertThat(response.body().jsonPath().getBoolean("menu_categories[0].menus[0].is_hidden"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenu().getIsHidden());
                softly.assertThat(response.body().jsonPath().getBoolean("menu_categories[0].menus[0].is_single"))
                    .isEqualTo(false);
                softly.assertThat(response.body().jsonPath().getString("menu_categories[0].menus[0].name"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenu().getName());
                softly.assertThat(
                        response.body().jsonPath().getString("menu_categories[0].menus[0].option_prices[0].option"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenu().getMenuOptions().get(0).getOption());
                softly.assertThat(
                        response.body().jsonPath().getInt("menu_categories[0].menus[0].option_prices[0].price"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenu().getMenuOptions().get(0).getPrice());
                softly.assertThat(
                        response.body().jsonPath().getString("menu_categories[0].menus[0].option_prices[1].option"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenu().getMenuOptions().get(1).getOption());
                softly.assertThat(
                        response.body().jsonPath().getInt("menu_categories[0].menus[0].option_prices[1].price"))
                    .isEqualTo(menu1.getMenuCategoryMaps().get(0).getMenu().getMenuOptions().get(1).getPrice());
                softly.assertThat((Object) response.body().jsonPath().get("menu_categories[0].menus[0].single_price"))
                    .isNull();
                softly.assertThat(response.body().jsonPath().getString("updated_at"))
                    .isEqualTo(LocalDate.now().toString());
            }
        );
    }

    @Test
    @DisplayName("사장님이 자신의 상점 메뉴 카테고리들을 조회한다.")
    void findOwnerMenuCategories() {
        // given
        final long SHOP_ID = 1L;

        Menu menu = Menu.builder()
            .shopId(SHOP_ID)
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();

        MenuCategory menuCategory1 = MenuCategory.builder()
            .shop(shop)
            .name("이벤트 메뉴")
            .build();

        MenuCategory menuCategory2 = MenuCategory.builder()
            .shop(shop)
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
            .param("shopId", SHOP_ID)
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/owner/shops/menus/categories")
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
    @DisplayName("사장님이 자신의 상점의 특정 메뉴를 조회한다.")
    void findMenuShopOwner() {
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
            .shop(shop)
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
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/owner/shops/menus/{menuId}", menu.getId())
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
    @DisplayName("권한이 없는 상점 사장님이 특정 상점 조회")
    void ownerCannotQueryOtherStoresWithoutPermission() {
        // given
        RestAssured
            .given()
            .header("Authorization", "Bearer " + otherOwnerToken)
            .when()
            .get("/owner/shops/{shopId}", shop.getId())
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("사장님이 메뉴 카테고리를 삭제한다.")
    void deleteMenuCategory() {
        // given
        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
            .name("중식")
            .build();
        MenuCategory svaedMenuCategory = menuCategoryRepository.save(menuCategory);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/owner/shops/menus/categories/{categoryId}", svaedMenuCategory.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        boolean isPresent = menuCategoryRepository.findById(svaedMenuCategory.getId()).isPresent();
        SoftAssertions.assertSoftly(softly -> softly.assertThat(isPresent).isFalse());
    }

    @Test
    @DisplayName("사장님이 메뉴를 삭제한다.")
    void deleteMenu() {
        // given
        Menu menu = Menu.builder()
            .shopId(1L)
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();
        Menu savedMenu = menuRepository.save(menu);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/owner/shops/menus/{menuId}", savedMenu.getId())
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        boolean isPresent = menuRepository.findById(savedMenu.getId()).isPresent();
        SoftAssertions.assertSoftly(softly -> softly.assertThat(isPresent).isFalse());
    }

    @Test
    @DisplayName("사장님이 옵션이 여러개인 메뉴를 추가한다.")
    void createManyOptionMenu() {
        // given
        MenuCategory menuCategory = MenuCategory.builder()
            .name("테스트")
            .shop(shop)
            .build();
        MenuCategory savedMenuCategory = menuCategoryRepository.save(menuCategory);
        String categoryId = savedMenuCategory.getId().toString();
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "category_ids": [
                    %s
                  ],
                  "description": "테스트메뉴입니다.",
                  "image_urls": [
                    "string"
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
                """, categoryId))
            .when()
            .post("/owner/shops/{id}/menus", shop.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Menu menu = menuRepository.getById(1L);
                assertSoftly(
                    softly -> {
                        List<MenuCategoryMap> menuCategoryMaps = menu.getMenuCategoryMaps();
                        List<MenuOption> menuOptions = menu.getMenuOptions();
                        List<MenuImage> menuImages = menu.getMenuImages();
                        softly.assertThat(menu.getDescription()).isEqualTo("테스트메뉴입니다.");
                        softly.assertThat(menu.getName()).isEqualTo("짜장면");

                        softly.assertThat(menuImages.get(0).getImageUrl()).isEqualTo("string");
                        softly.assertThat(menuCategoryMaps.get(0).getMenuCategory().getId()).isEqualTo(1L);

                        softly.assertThat(menuOptions.get(0).getOption()).isEqualTo("중");
                        softly.assertThat(menuOptions.get(0).getPrice()).isEqualTo(10000);

                        softly.assertThat(menuOptions.get(1).getOption()).isEqualTo("소");
                        softly.assertThat(menuOptions.get(1).getPrice()).isEqualTo(5000);

                    }
                );
            }
        });
    }

    @Test
    @DisplayName("사장님이 옵션이 한개인 메뉴를 추가한다.")
    void createOneOptionMenu() {
        // given
        MenuCategory menuCategory = MenuCategory.builder()
            .name("테스트")
            .shop(shop)
            .build();
        MenuCategory savedMenuCategory = menuCategoryRepository.save(menuCategory);
        String categoryId = savedMenuCategory.getId().toString();
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "category_ids": [
                    %s
                  ],
                  "description": "테스트메뉴입니다.",
                  "image_urls": [
                    "string"
                  ],
                  "is_single": true,
                  "name": "짜장면",
                  "option_prices": null,
                  "single_price": 10000
                }
                """, categoryId))
            .when()
            .post("/owner/shops/{id}/menus", shop.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Menu menu = menuRepository.getById(1L);
                assertSoftly(
                    softly -> {
                        List<MenuCategoryMap> menuCategoryMaps = menu.getMenuCategoryMaps();
                        List<MenuOption> menuOptions = menu.getMenuOptions();
                        List<MenuImage> menuImages = menu.getMenuImages();
                        softly.assertThat(menu.getDescription()).isEqualTo("테스트메뉴입니다.");
                        softly.assertThat(menu.getName()).isEqualTo("짜장면");

                        softly.assertThat(menuImages.get(0).getImageUrl()).isEqualTo("string");
                        softly.assertThat(menuCategoryMaps.get(0).getMenuCategory().getId()).isEqualTo(1L);

                        softly.assertThat(menuOptions.get(0).getPrice()).isEqualTo(10000);
                    }
                );
            }
        });
    }

    @Test
    @DisplayName("사장님이 메뉴 카테고리를 추가한다.")
    void createMenuCategory() {
        // given
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                   "name": "사이드 메뉴"
                }
                """))
            .when()
            .post("/owner/shops/{id}/menus/categories", shop.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        MenuCategory menuCategory = menuCategoryRepository.getById(1L);

        assertSoftly(
            softly -> softly.assertThat(menuCategory.getName()).isEqualTo("사이드 메뉴")
        );
    }

    @Test
    @DisplayName("사장님이 단일 메뉴로 수정한다.")
    void modifyOneMenu() {
        // given
        Menu createdMenu = createMenu();

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "category_ids": [
                    2
                  ],
                  "description": "테스트메뉴수정",
                  "image_urls": [
                    "string2"
                  ],
                  "is_single": true,
                  "name": "짜장면2",
                  "single_price": 10000
                }
                """))
            .when()
            .put("/owner/shops/menus/{menuId}", createdMenu.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Menu menu = menuRepository.getById(1L);
                assertSoftly(
                    softly -> {
                        List<MenuCategoryMap> menuCategoryMaps = menu.getMenuCategoryMaps();
                        List<MenuOption> menuOptions = menu.getMenuOptions();
                        List<MenuImage> menuImages = menu.getMenuImages();
                        softly.assertThat(menu.getDescription()).isEqualTo("테스트메뉴수정");
                        softly.assertThat(menu.getName()).isEqualTo("짜장면2");

                        softly.assertThat(menuImages.get(0).getImageUrl()).isEqualTo("string2");
                        softly.assertThat(menuCategoryMaps.get(0).getMenuCategory().getId()).isEqualTo(2L);

                        softly.assertThat(menuOptions.get(0).getPrice()).isEqualTo(10000);

                    }
                );
            }
        });
    }

    @Test
    @DisplayName("사장님이 여러옵션을 가진 메뉴로 수정한다.")
    void modifyManyOptionMenu() {
        // given
        Menu createdMenu = createMenu();

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "category_ids": [
                    1, 2
                  ],
                  "description": "테스트메뉴입니다.",
                  "image_urls": [
                    "string"
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
                """))
            .when()
            .put("/owner/shops/menus/{menuId}", createdMenu.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Menu menu = menuRepository.getById(1L);
                assertSoftly(
                    softly -> {
                        List<MenuCategoryMap> menuCategoryMaps = menu.getMenuCategoryMaps();
                        List<MenuOption> menuOptions = menu.getMenuOptions();
                        List<MenuImage> menuImages = menu.getMenuImages();
                        softly.assertThat(menu.getDescription()).isEqualTo("테스트메뉴입니다.");
                        softly.assertThat(menu.getName()).isEqualTo("짜장면");

                        softly.assertThat(menuImages.get(0).getImageUrl()).isEqualTo("string");
                        softly.assertThat(menuCategoryMaps.get(0).getMenuCategory().getId()).isEqualTo(1L);
                        softly.assertThat(menuCategoryMaps.get(1).getMenuCategory().getId()).isEqualTo(2L);

                        softly.assertThat(menuOptions.get(0).getOption()).isEqualTo("중");
                        softly.assertThat(menuOptions.get(0).getPrice()).isEqualTo(10000);

                        softly.assertThat(menuOptions.get(1).getOption()).isEqualTo("소");
                        softly.assertThat(menuOptions.get(1).getPrice()).isEqualTo(5000);

                    }
                );
            }
        });
    }

    @Test
    @DisplayName("사장님이 상점을 수정한다.")
    void modifyShop() {
        // given
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "address": "충청남도 천안시 동남구 병천면 충절로 1600",
                  "category_ids": [
                   1, 2
                  ],
                  "delivery": false,
                  "delivery_price": 1000,
                  "description": "이번주 전 메뉴 10% 할인 이벤트합니다.",
                  "image_urls": [
                    "string2"
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
                """)
            .when()
            .put("/owner/shops/{id}", shop.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Shop modifiedShop = shopRepository.getById(1L);
                List<ShopImage> shopImages = modifiedShop.getShopImages();
                List<ShopOpen> shopOpens = modifiedShop.getShopOpens();
                List<ShopCategoryMap> shopCategoryMaps = modifiedShop.getShopCategories();
                assertSoftly(
                    softly -> {
                        softly.assertThat(modifiedShop.getAddress()).isEqualTo("충청남도 천안시 동남구 병천면 충절로 1600");
                        softly.assertThat(modifiedShop.getIsDeleted()).isFalse();
                        softly.assertThat(modifiedShop.getDeliveryPrice()).isEqualTo(1000);
                        softly.assertThat(modifiedShop.getDescription()).isEqualTo("이번주 전 메뉴 10% 할인 이벤트합니다.");
                        softly.assertThat(modifiedShop.getName()).isEqualTo("써니 숯불 도시락");
                        softly.assertThat(modifiedShop.getPayBank()).isTrue();
                        softly.assertThat(modifiedShop.getPayCard()).isTrue();
                        softly.assertThat(modifiedShop.getPhone()).isEqualTo("041-123-4567");

                        softly.assertThat(shopCategoryMaps.get(0).getShopCategory().getId()).isEqualTo(1L);
                        softly.assertThat(shopCategoryMaps.get(1).getShopCategory().getId()).isEqualTo(2L);

                        softly.assertThat(shopImages.get(0).getImageUrl()).isEqualTo("string2");

                        softly.assertThat(shopOpens.get(0).getCloseTime()).isEqualTo("22:30");
                        softly.assertThat(shopOpens.get(0).getOpenTime()).isEqualTo("10:00");

                        softly.assertThat(shopOpens.get(0).getDayOfWeek()).isEqualTo("MONDAY");
                        softly.assertThat(shopOpens.get(0).getClosed()).isFalse();

                        softly.assertThat(shopOpens.get(1).getCloseTime()).isEqualTo("23:30");
                        softly.assertThat(shopOpens.get(1).getOpenTime()).isEqualTo("11:00");

                        softly.assertThat(shopOpens.get(1).getDayOfWeek()).isEqualTo("SUNDAY");
                        softly.assertThat(shopOpens.get(1).getClosed()).isTrue();
                    }
                );
            }
        });
    }

    private Menu createMenu() {
        MenuCategory menuCategory1 = MenuCategory.builder()
            .name("테스트")
            .shop(shop)
            .build();
        MenuCategory menuCategory2 = MenuCategory.builder()
            .name("테스트")
            .shop(shop)
            .build();
        MenuCategory savedMenuCategory = menuCategoryRepository.save(menuCategory1);
        menuCategoryRepository.save(menuCategory2);
        String categoryId = savedMenuCategory.getId().toString();
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "category_ids": [
                    %s
                  ],
                  "description": "테스트메뉴입니다.",
                  "image_urls": [
                    "string"
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
                """, categoryId))
            .when()
            .post("/owner/shops/{id}/menus", shop.getId())
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();
        return menuRepository.getById(1L);
    }

    @Test
    @DisplayName("권한이 없는 상점 사장님이 특정 카테고리 조회한다.")
    void ownerCannotQueryOtherCategoriesWithoutPermission() {
        // given
        RestAssured
            .given()
            .param("shopId", 1)
            .header("Authorization", "Bearer " + otherOwnerToken)
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
            .header("Authorization", "Bearer " + otherOwnerToken)
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
        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
            .name("중식")
            .build();
        MenuCategory svaedMenuCategory = menuCategoryRepository.save(menuCategory);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + otherOwnerToken)
            .when()
            .delete("/owner/shops/menus/categories/{categoryId}", svaedMenuCategory.getId())
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("권한이 없는 사장님이 메뉴를 삭제한다.")
    void ownerCannotDeleteOtherMenusWithoutPermission() {
        // given
        Menu menu = Menu.builder()
            .shopId(1L)
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();
        Menu savedMenu = menuRepository.save(menu);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + otherOwnerToken)
            .when()
            .delete("/owner/shops/menus/{menuId}", savedMenu.getId())
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("사장님이 이벤트를 추가한다.")
    void ownerShopCreateEvent() {
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "title": "감성떡볶이 이벤트합니다!",
                  "content": "테스트 이벤트입니다.",
                  "thumbnail_image": "https://test.com/test1.jpg",
                  "start_date": "2024-10-24",
                  "end_date": "2024-10-26"
                }
                """)
            .when()
            .post("/owner/shops/{shopId}/event", shop.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        EventArticle eventArticle = eventArticleRepository.getById(1L);

        assertSoftly(
            softly -> {
                softly.assertThat(eventArticle.getShop().getId()).isEqualTo(1L);
                softly.assertThat(eventArticle.getTitle()).isEqualTo("감성떡볶이 이벤트합니다!");
                softly.assertThat(eventArticle.getContent()).isEqualTo("테스트 이벤트입니다.");
                softly.assertThat(eventArticle.getThumbnail()).isEqualTo("https://test.com/test1.jpg");
                softly.assertThat(eventArticle.getStartDate().toString()).isEqualTo("2024-10-24");
                softly.assertThat(eventArticle.getEndDate().toString()).isEqualTo("2024-10-26");
            }
        );
    }

    @Test
    @DisplayName("사장님이 이벤트를 수정한다.")
    void ownerShopModifyEvent() {
        EventArticle eventArticle = EventArticle.builder()
            .shop(shop)
            .title("테스트 제목1")
            .content("테스트 내용1")
            .ip("")
            .startDate(LocalDate.of(2024,10,24))
            .endDate(LocalDate.of(2024,10,26))
            .thumbnail("https://test.com/test1.jpg")
            .hit(0)
            .build();
        EventArticle savedEvent = eventArticleRepository.save(eventArticle);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "title": "감성떡볶이 이벤트합니다!",
                  "content": "테스트 이벤트입니다.",
                  "thumbnail_image": "https://test.com/test1.jpg",
                  "start_date": "2024-10-24",
                  "end_date": "2024-10-26"
                }
                """)
            .when()
            .put("/owner/shops/{shopId}/event/{eventId}", shop.getId(), savedEvent.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        EventArticle modifiedEventArticle = eventArticleRepository.getById(savedEvent.getId());

        assertSoftly(
            softly -> {
                softly.assertThat(modifiedEventArticle.getShop().getId()).isEqualTo(1L);
                softly.assertThat(modifiedEventArticle.getTitle()).isEqualTo("감성떡볶이 이벤트합니다!");
                softly.assertThat(modifiedEventArticle.getContent()).isEqualTo("테스트 이벤트입니다.");
                softly.assertThat(modifiedEventArticle.getThumbnail()).isEqualTo("https://test.com/test1.jpg");
                softly.assertThat(modifiedEventArticle.getStartDate().toString()).isEqualTo("2024-10-24");
                softly.assertThat(modifiedEventArticle.getEndDate().toString()).isEqualTo("2024-10-26");
            }
        );
    }

    @Test
    @DisplayName("사장님이 이벤트를 삭제한다.")
    void ownerShopDeleteEvent() {
        EventArticle eventArticle = EventArticle.builder()
            .shop(shop)
            .title("테스트 제목1")
            .content("테스트 내용1")
            .ip("")
            .startDate(LocalDate.of(2024,10,24))
            .endDate(LocalDate.of(2024,10,26))
            .thumbnail("https://test.com/test1.jpg")
            .hit(0)
            .build();
        EventArticle savedEvent = eventArticleRepository.save(eventArticle);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .when()
            .delete("/owner/shops/{shopId}/event/{eventId}", shop.getId(), savedEvent.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        Optional<EventArticle> modifiedEventArticle = eventArticleRepository.findById(savedEvent.getId());

        assertSoftly(softly -> softly.assertThat(modifiedEventArticle.isPresent()).isFalse());
    }
}
