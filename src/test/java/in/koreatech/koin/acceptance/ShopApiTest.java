package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
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
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.shop.model.EventArticle;
import in.koreatech.koin.domain.shop.model.EventArticleImage;
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
import in.koreatech.koin.domain.shop.repository.EventArticleImageRepository;
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
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class ShopApiTest extends AcceptanceTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

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
    private EventArticleImageRepository eventArticleImageRepository;

    @Autowired
    private ShopCategoryRepository shopCategoryRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private EventArticleRepository eventArticleRepository;

    private ShopCategory shopCategory1, shopCategory2;
    private Shop shop;
    private Owner owner;

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
            .companyRegistrationNumber("123-45-67892")
            .attachments(List.of(attachment))
            .grantShop(true)
            .grantEvent(true)
            .user(
                User.builder()
                    .password("1234")
                    .nickname("셋업유저")
                    .name("셋업")
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
            .deliveryPrice(3000)
            .payCard(true)
            .payBank(true)
            .isDeleted(false)
            .isEvent(false)
            .remarks("비고")
            .hit(0)
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
            .shopId(1)
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();

        MenuCategory menuCategory = MenuCategory.builder()
            .shop(shop)
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
                softly.assertThat(response.body().jsonPath().getInt("id")).isEqualTo(menu.getId());

                softly.assertThat(response.body().jsonPath().getInt("shop_id")).isEqualTo(menu.getShopId());
                softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo(menu.getName());
                softly.assertThat(response.body().jsonPath().getBoolean("is_hidden")).isEqualTo(menu.isHidden());

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
            .shopId(1)
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
            .when()
            .get("/shops/{shopId}/menus/{menuId}", menu.getShopId(), menu.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .log().all()
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                        "https://test.com/hello.jpg",
                        "https://test.com/test.jpg"
                    ]
                }
                """);
    }

    @Test
    @DisplayName("상점의 메뉴 카테고리들을 조회한다.")
    void findShopMenuCategories() {
        // given
        final int SHOP_ID = 1;

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
            .when()
            .get("/shops/{shopId}/menus/categories", menu.getShopId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getInt("count")).isEqualTo(2);

                softly.assertThat(response.body().jsonPath().getList("menu_categories"))
                    .hasSize(2);

                softly.assertThat(response.body().jsonPath().getInt("menu_categories[0].id"))
                    .isEqualTo(menuCategory1.getId());
                softly.assertThat(response.body().jsonPath().getString("menu_categories[0].name"))
                    .isEqualTo(menuCategory1.getName());

                softly.assertThat(response.body().jsonPath().getInt("menu_categories[1].id"))
                    .isEqualTo(menuCategory2.getId());
                softly.assertThat(response.body().jsonPath().getString("menu_categories[1].name"))
                    .isEqualTo(menuCategory2.getName());
            }
        );
    }

    @Test
    @DisplayName("특정 상점 조회")
    void getShop() throws Exception {
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
            .get("/shops/{shopId}", shop.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

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
                }""", LocalDateTime.now().format(ofPattern("yyyy-MM-dd")))
            );
    }

    @Test
    @DisplayName("특정 상점 모든 메뉴 조회")
    void getShopMenus() {
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

        Menu menu1 = Menu.builder()
            .shopId(1)
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

        MenuOption menuOption3 = MenuOption.builder()
            .option("일반")
            .price(7000)
            .build();

        MenuOption menuOption4 = MenuOption.builder()
            .option("곱빼기")
            .price(7500)
            .build();

        MenuImage menuImage3 = MenuImage.builder()
            .imageUrl("https://test.com/test.jpg")
            .build();
        MenuImage menuImage4 = MenuImage.builder()
            .imageUrl("https://test.com/hello.jpg")
            .build();

        Menu menu2 = Menu.builder()
            .shopId(1)
            .name("짜장면2")
            .description("맛있는 짜장면")
            .build();

        MenuCategory menuCategory2 = MenuCategory.builder()
            .shop(shop)
            .name("한식")
            .build();

        MenuCategoryMap menuCategoryMap2 = MenuCategoryMap.create();
        menuCategoryRepository.save(menuCategory2);

        menuOption3.setMenu(menu2);
        menuOption4.setMenu(menu2);
        menuImage3.setMenu(menu2);
        menuImage4.setMenu(menu2);

        menuCategoryMap2.map(menu2, menuCategory2);

        menuRepository.save(menu2);

        MenuOption menuOption5 = MenuOption.builder()
            .option("일반")
            .price(7000)
            .build();

        MenuImage menuImage5 = MenuImage.builder()
            .imageUrl("https://test.com/test.jpg")
            .build();
        MenuImage menuImage6 = MenuImage.builder()
            .imageUrl("https://test.com/hello.jpg")
            .build();

        Menu menu3 = Menu.builder()
            .shopId(1)
            .name("짜장면3")
            .description("맛있는 짜장면")
            .build();

        MenuCategoryMap menuCategoryMap3 = MenuCategoryMap.create();

        menuOption5.setMenu(menu3);
        menuImage5.setMenu(menu3);
        menuImage6.setMenu(menu3);

        menuCategoryMap3.map(menu3, menuCategory2);

        menuRepository.save(menu3);

        MenuCategory menuCategory3 = MenuCategory.builder()
            .shop(shop)
            .name("이벤트")
            .build();

        menuCategoryRepository.save(menuCategory3);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/menus", shop.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .log().all()
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                    {
                        "count": 3,
                        "menu_categories": [
                            {
                                "id": 1,
                                "name": "중식",
                                "menus": [
                                    {
                                        "id": 1,
                                        "name": "짜장면",
                                        "is_hidden": false,
                                        "is_single": false,
                                        "single_price": null,
                                        "option_prices": [
                                            {
                                                "option": "일반",
                                                "price": 7000
                                            },
                                            {
                                                "option": "곱빼기",
                                                "price": 7500
                                            }
                                        ],
                                        "description": "맛있는 짜장면",
                                        "image_urls": [
                                            "https://test.com/test.jpg",
                                            "https://test.com/hello.jpg"
                                        ]
                                    }
                                ]
                            },
                            {
                                "id": 2,
                                "name": "한식",
                                "menus": [
                                    {
                                        "id": 2,
                                        "name": "짜장면2",
                                        "is_hidden": false,
                                        "is_single": false,
                                        "single_price": null,
                                        "option_prices": [
                                            {
                                                "option": "일반",
                                                "price": 7000
                                            },
                                            {
                                                "option": "곱빼기",
                                                "price": 7500
                                            }
                                        ],
                                        "description": "맛있는 짜장면",
                                        "image_urls": [
                                            "https://test.com/test.jpg",
                                            "https://test.com/hello.jpg"
                                        ]
                                    },
                                    {
                                        "id": 3,
                                        "name": "짜장면3",
                                        "is_hidden": false,
                                        "is_single": true,
                                        "single_price": 7000,
                                        "option_prices": null,
                                        "description": "맛있는 짜장면",
                                        "image_urls": [
                                            "https://test.com/test.jpg",
                                            "https://test.com/hello.jpg"
                                        ]
                                    }
                                ]
                            }
                        ],
                        "updated_at": %s
                    }
                    """,
                response.jsonPath().getString("updated_at"))
            );
    }

    @Test
    @DisplayName("모든 상점 조회")
    void getAllShop() {
        // given
        Shop shopRequest = Shop.builder()
            .owner(owner)
            .name("테스트 상점2")
            .internalName("테스트2")
            .chosung("테스트")
            .phone("010-1234-5678")
            .address("대전광역시 유성구 대학로 291")
            .description("테스트 상점입니다.")
            .delivery(true)
            .deliveryPrice(3000)
            .payCard(true)
            .payBank(true)
            .isDeleted(false)
            .isEvent(false)
            .remarks("비고")
            .hit(0)
            .build();
        Shop newShop = shopRepository.save(shopRequest);

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
            .shop(newShop)
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
            .shop(newShop)
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
            .shop(newShop)
            .build();

        shopImageRepository.save(shopImage1);
        shopImageRepository.save(shopImage2);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<Shop> shops = shopRepository.findAll();
                Shop shop1 = shops.get(0);
                Shop shop2 = shops.get(1);
                assertSoftly(
                    softly -> {
                        softly.assertThat(response.body().jsonPath().getString("count")).isEqualTo("2");
                        softly.assertThat(response.body().jsonPath().getBoolean("shops[0].delivery"))
                            .isEqualTo(shop1.isDelivery());
                        softly.assertThat(response.body().jsonPath().getInt("shops[0].id")).isEqualTo(shop1.getId());
                        softly.assertThat(response.body().jsonPath().getString("shops[0].name"))
                            .isEqualTo(shop1.getName());
                        softly.assertThat(response.body().jsonPath().getBoolean("shops[0].pay_bank"))
                            .isEqualTo(shop1.isPayBank());
                        softly.assertThat(response.body().jsonPath().getBoolean("shops[0].pay_card"))
                            .isEqualTo(shop1.isPayCard());
                        softly.assertThat(response.body().jsonPath().getString("shops[0].phone"))
                            .isEqualTo(shop1.getPhone());
                        softly.assertThat(response.body().jsonPath().getList("shops[0].category_ids")).hasSize(1);
                        softly.assertThat(response.body().jsonPath().getList("shops[0].open")).hasSize(1);

                        softly.assertThat(response.body().jsonPath().getBoolean("shops[1].delivery"))
                            .isEqualTo(shop2.isDelivery());
                        softly.assertThat(response.body().jsonPath().getInt("shops[1].id")).isEqualTo(shop2.getId());
                        softly.assertThat(response.body().jsonPath().getString("shops[1].name"))
                            .isEqualTo(shop2.getName());
                        softly.assertThat(response.body().jsonPath().getBoolean("shops[1].pay_bank"))
                            .isEqualTo(shop2.isPayBank());
                        softly.assertThat(response.body().jsonPath().getBoolean("shops[1].pay_card"))
                            .isEqualTo(shop2.isPayCard());
                        softly.assertThat(response.body().jsonPath().getString("shops[1].phone"))
                            .isEqualTo(shop2.getPhone());
                        softly.assertThat(response.body().jsonPath().getList("shops[1].category_ids")).hasSize(1);
                        softly.assertThat(response.body().jsonPath().getList("shops[1].open")).hasSize(1);
                    }
                );
            }
        });
    }

    @Test
    @DisplayName("상점들의 모든 카테고리를 조회한다.")
    void getAllShopCategories() {
        // given
        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops/categories")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();
        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getInt("total_count")).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getInt("shop_categories[0].id")).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getString("shop_categories[0].name")).isEqualTo("테스트1");
                softly.assertThat(response.body().jsonPath().getString("shop_categories[0].image_url"))
                    .isEqualTo("https://test.com/test1.jpg");
                softly.assertThat(response.body().jsonPath().getInt("shop_categories[1].id")).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getString("shop_categories[1].name")).isEqualTo("테스트2");
                softly.assertThat(response.body().jsonPath().getString("shop_categories[1].image_url"))
                    .isEqualTo("https://test.com/test2.jpg");
            }
        );
    }

    @Test
    @DisplayName("특정 상점의 이벤트들을 조회한다.")
    void getShopEvents() {
        when(clock.instant()).thenReturn(
            ZonedDateTime.parse("2024-02-21 18:00:00 KST", ofPattern("yyyy-MM-dd HH:mm:ss z")).toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        var now = LocalDate.of(2024, 2, 21);
        var user = owner.getUser();
        Shop shopRequest = Shop.builder()
            .owner(owner)
            .name("테스트 상점2")
            .internalName("테스트2")
            .chosung("테스트")
            .phone("010-1234-5678")
            .address("대전광역시 유성구 대학로 291")
            .description("테스트 상점입니다.")
            .delivery(true)
            .deliveryPrice(3000)
            .payCard(true)
            .payBank(true)
            .isDeleted(false)
            .isEvent(false)
            .remarks("비고")
            .hit(0)
            .build();
        Shop newShop = shopRepository.save(shopRequest);

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
            .shop(newShop)
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
            .shop(newShop)
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
            .shop(newShop)
            .build();

        shopImageRepository.save(shopImage1);
        shopImageRepository.save(shopImage2);

        EventArticle eventArticle1 = createEventArticle(
            newShop,
            "테스트 이벤트 1",
            "<P>테스트 이벤트 내용1</P>",
            LocalDate.now().minusDays(3),
            LocalDate.now().plusDays(3),
            List.of("https://test.com/test-thumbnail-1.jpg")
        );

        EventArticle eventArticle2 = createEventArticle(
            newShop,
            "테스트 이벤트 2",
            "<P>테스트 이벤트 내용2</P>",
            LocalDate.now().minusDays(3),
            LocalDate.now().plusDays(3),
            List.of("https://test.com/test-thumbnail-2.jpg")
        );

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/events", newShop.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(String.format("""
                {
                   "events": [
                       {
                            "shop_id": %s,
                            "shop_name": "%s",
                            "event_id": %s,
                            "title": "테스트 이벤트 1",
                            "content": "<P>테스트 이벤트 내용1</P>",
                            "thumbnail_images": [
                                "https://test.com/test-thumbnail-1.jpg"
                            ],
                            "start_date": "%s",
                            "end_date": "%s"
                       },
                       {
                            "shop_id": %s,
                            "shop_name": "%s",
                            "event_id": %s,
                            "title": "테스트 이벤트 2",
                            "content": "<P>테스트 이벤트 내용2</P>",
                            "thumbnail_images": [
                                "https://test.com/test-thumbnail-2.jpg"
                            ],
                            "start_date": "%s",
                            "end_date": "%s"
                       }
                   ]
                   }""",
            newShop.getId(),
            newShop.getName(),
            eventArticle1.getId(),
            LocalDate.now().minusDays(3),
            LocalDate.now().plusDays(3),
            newShop.getId(),
            newShop.getName(),
            eventArticle2.getId(),
            LocalDate.now().minusDays(3),
            LocalDate.now().plusDays(3))).isEqualTo(response.asPrettyString());
    }

    @Test
    @DisplayName("이벤트 진행중인 상점의 정보를 조회한다.")
    void getShopWithEvents() {
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        var now = LocalDate.of(2024, 2, 21);
        var user = owner.getUser();
        Shop shopRequest = Shop.builder()
            .owner(owner)
            .name("테스트 상점2")
            .internalName("테스트2")
            .chosung("테스트")
            .phone("010-1234-5678")
            .address("대전광역시 유성구 대학로 291")
            .description("테스트 상점입니다.")
            .delivery(true)
            .deliveryPrice(3000)
            .payCard(true)
            .payBank(true)
            .isDeleted(false)
            .isEvent(false)
            .remarks("비고")
            .hit(0)
            .build();
        Shop newShop = shopRepository.save(shopRequest);

        ShopOpen open1 = ShopOpen.builder()
            .openTime(LocalTime.of(0, 0))
            .closeTime(LocalTime.of(21, 0))
            .shop(newShop)
            .closed(false)
            .dayOfWeek("MONDAY")
            .build();

        ShopOpen open2 = ShopOpen.builder()
            .openTime(LocalTime.of(0, 0))
            .closeTime(LocalTime.of(0, 0))
            .shop(newShop)
            .closed(false)
            .dayOfWeek("FRIDAY")
            .build();

        shopOpenRepository.save(open1);
        shopOpenRepository.save(open2);

        ShopCategoryMap shopCategoryMap1 = ShopCategoryMap.builder()
            .shop(newShop)
            .shopCategory(shopCategory1)
            .build();

        ShopCategoryMap shopCategoryMap2 = ShopCategoryMap.builder()
            .shop(newShop)
            .shopCategory(shopCategory2)
            .build();

        shopCategoryMapRepository.save(shopCategoryMap1);
        shopCategoryMapRepository.save(shopCategoryMap2);

        ShopImage shopImage1 = ShopImage.builder()
            .imageUrl("https://test.com/test1.jpg")
            .shop(newShop)
            .build();

        ShopImage shopImage2 = ShopImage.builder()
            .imageUrl("https://test.com/test2.jpg")
            .shop(newShop)
            .build();

        shopImageRepository.save(shopImage1);
        shopImageRepository.save(shopImage2);

        EventArticle eventArticle1 = createEventArticle(
            newShop,
            "테스트 이벤트 1",
            "<P>테스트 이벤트 내용1</P>",
            LocalDate.now().minusDays(3),
            LocalDate.now().plusDays(3),
            List.of("https://test.com/test-thumbnail-1.jpg")
        );

        EventArticle eventArticle2 = createEventArticle(
            newShop,
            "테스트 이벤트 2",
            "<P>테스트 이벤트 내용2</P>",
            LocalDate.now().minusDays(3),
            LocalDate.now().plusDays(3),
            List.of("https://test.com/test-thumbnail-2.jpg")
        );

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}", newShop.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Assertions.assertThat(response.jsonPath().getBoolean("is_event")).isTrue();
    }

    @Test
    @DisplayName("이벤트 진행중이지 않은 상점의 정보를 조회한다.")
    void getShopWithoutEvents() {
        when(clock.instant()).thenReturn(
            ZonedDateTime.parse("2024-02-21 18:00:00 KST", ofPattern("yyyy-MM-dd HH:mm:ss z")).toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());

        var now = LocalDate.of(2024, 2, 21);
        var user = owner.getUser();
        Shop shopRequest = Shop.builder()
            .owner(owner)
            .name("테스트 상점2")
            .internalName("테스트2")
            .chosung("테스트")
            .phone("010-1234-5678")
            .address("대전광역시 유성구 대학로 291")
            .description("테스트 상점입니다.")
            .delivery(true)
            .deliveryPrice(3000)
            .payCard(true)
            .payBank(true)
            .isDeleted(false)
            .isEvent(false)
            .remarks("비고")
            .hit(0)
            .build();
        Shop newShop = shopRepository.save(shopRequest);

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
            .shop(newShop)
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
            .shop(newShop)
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
            .shop(newShop)
            .build();

        shopImageRepository.save(shopImage1);
        shopImageRepository.save(shopImage2);

        List<String> thumbnailImages = List.of("https://test.com/test-thumbnail-1.jpg");

        EventArticle eventArticle1 = createEventArticle(
            newShop,
            "테스트 이벤트 1",
            "<P>테스트 이벤트 내용1</P>",
            LocalDate.now().minusDays(5),
            LocalDate.now().minusDays(1),
            List.of("https://test.com/test-thumbnail-1.jpg")
        );

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}", newShop.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Assertions.assertThat(response.jsonPath().getBoolean("is_event")).isFalse();
    }

    @Test
    @DisplayName("이벤트 베너 조회")
    void ownerShopDeleteEvent() {
        EventArticle savedEvent1 = createEventArticle(
            shop,
            "테스트 제목1",
            "테스트 내용1",
            LocalDate.now(),
            LocalDate.now().plusDays(10),
            List.of("https://test.com/test1.jpg")
        );
        EventArticle savedEvent2 = createEventArticle(
            shop,
            "테스트 제목1",
            "테스트 내용1",
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            List.of("https://test.com/test1.jpg")
        );
        EventArticle savedEvent3 = createEventArticle(
            shop,
            "테스트 제목3",
            "테스트 내용3",
            LocalDate.now().minusDays(10),
            LocalDate.now().plusDays(10),
            List.of("https://test.com/test1.jpg")
        );
        ExtractableResponse<Response> response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .when()
            .get("/shops/events")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                    {
                        "events": [
                            {
                                "shop_id": %s,
                                "shop_name": "%s",
                                "event_id": %s,
                                "title": "테스트 제목1",
                                "content": "테스트 내용1",
                                "thumbnail_images": [
                                    "https://test.com/test1.jpg"
                                ],
                                "start_date": %s,
                                "end_date": %s
                            },
                            {
                                "shop_id": %s,
                                "shop_name": "%s",
                                "event_id": %s,
                                "title": "테스트 제목3",
                                "content": "테스트 내용3",
                                "thumbnail_images": [
                                    "https://test.com/test1.jpg"
                                ],
                                "start_date": %s,
                                "end_date": %s
                            }
                        ]
                    }""",
                shop.getId(),
                shop.getName(),
                savedEvent1.getId(),
                response.jsonPath().getString("events[0].start_date"),
                response.jsonPath().getString("events[0].end_date"),
                shop.getId(),
                shop.getName(),
                savedEvent3.getId(),
                response.jsonPath().getString("events[1].start_date"),
                response.jsonPath().getString("events[1].end_date")));
    }

    private EventArticle createEventArticle(
        Shop shop,
        String title,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        List<String> thumbnailImages
    ) {
        EventArticle eventArticle = EventArticle.builder()
            .shop(shop)
            .title(title)
            .content(content)
            .ip("")
            .startDate(startDate)
            .endDate(endDate)
            .hit(0)
            .build();
        EventArticle savedEvent = eventArticleRepository.save(eventArticle);
        for (String image : thumbnailImages) {
            EventArticleImage eventArticleImage = EventArticleImage.builder()
                .eventArticle(eventArticle)
                .thumbnailImage(image)
                .build();
            eventArticleImageRepository.save(eventArticleImage);
        }
        return eventArticleRepository.getById(savedEvent.getId());
    }
}
