package in.koreatech.koin.acceptance;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.Shop;
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
class ShopApiTest extends AcceptanceTest {

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ShopFixture shopFixture;

    @Autowired
    private MenuFixture menuFixture;

    @Autowired
    private MenuCategoryFixture menuCategoryFixture;

    @Autowired
    private EventArticleFixture eventArticleFixture;

    @Autowired
    private ShopCategoryFixture shopCategoryFixture;

    private Shop shop;
    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = userFixture.준영_사장님();
        shop = shopFixture.마슬랜(owner);
    }

    @Test
    @DisplayName("옵션이 하나 있는 상점의 메뉴를 조회한다.")
    void findMenuSingleOption() {
        // given
        Menu menu = menuFixture.짜장면_단일메뉴(shop, menuCategoryFixture.메인메뉴(shop));

        var response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/menus/{menuId}", menu.getShopId(), menu.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                """
            );
    }

    @Test
    @DisplayName("옵션이 여러 개 있는 상점의 메뉴를 조회한다.")
    void findMenuMultipleOption() {
        // given
        Menu menu = menuFixture.짜장면_옵션메뉴(shop, menuCategoryFixture.메인메뉴(shop));

        var response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/menus/{menuId}", menu.getShopId(), menu.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
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
                        "https://test.com/짜장면.jpg",
                        "https://test.com/짜장면22.jpg"
                    ]
                }
                HTTP/1.1 200\s
                Vary: Origin
                Vary: Access-Control-Request-Method
                Vary: Access-Control-Request-Headers
                Content-Type: application/json
                Transfer-Encoding: chunked
                Date: Mon, 22 Apr 2024 15:59:58 GMT
                Keep-Alive: timeout=60
                Connection: keep-alive
                                
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
                """);
    }

    @Test
    @DisplayName("상점의 메뉴 카테고리들을 조회한다.")
    void findShopMenuCategories() {
        // given
        menuCategoryFixture.사이드메뉴(shop);
        menuCategoryFixture.세트메뉴(shop);
        Menu menu = menuFixture.짜장면_단일메뉴(shop, menuCategoryFixture.추천메뉴(shop));

        var response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/menus/categories", menu.getShopId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                """);
    }

    @Test
    @DisplayName("특정 상점 조회")
    void getShop() {
        // given
        menuCategoryFixture.사이드메뉴(shop);
        menuCategoryFixture.세트메뉴(shop);
        var response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}", shop.getId())
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
                       
                    ],
                    "updated_at": "2024-01-15",
                    "is_event": false
                }
                """
            );
    }

    @Test
    @DisplayName("특정 상점 모든 메뉴 조회")
    void getShopMenus() {
        menuFixture.짜장면_단일메뉴(shop, menuCategoryFixture.추천메뉴(shop));
        menuFixture.짜장면_옵션메뉴(shop, menuCategoryFixture.세트메뉴(shop));
        var response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/menus", shop.getId())
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
                """
            );
    }

    @Test
    @DisplayName("모든 상점 조회")
    void getAllShop() {
        // given
        shopFixture.신전_떡볶이(owner);
        var response = RestAssured
            .given()
            .when()
            .get("/shops")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        // 2024-01-15 월요일 기준
        boolean 마슬랜_영업여부 = true;
        boolean 신전_떡볶이_영업여부 = false;

        System.out.println(LocalDateTime.now(clock));
        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
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
                                    "day_of_week": "SUNDAY",
                                    "closed": false,
                                    "open_time": "00:00",
                                    "close_time": "21:00"
                                },
                                {
                                    "day_of_week": "FRIDAY",
                                    "closed": false,
                                    "open_time": "00:00",
                                    "close_time": "21:00"
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
                """, 마슬랜_영업여부, 신전_떡볶이_영업여부));
    }

    @Test
    @DisplayName("상점들의 모든 카테고리를 조회한다.")
    void getAllShopCategories() {
        // given
        shopCategoryFixture.카테고리_일반음식();
        shopCategoryFixture.카테고리_치킨();
        var response = RestAssured
            .given()
            .when()
            .get("/shops/categories")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                     "total_count": 2,
                     "shop_categories": [
                         {
                             "id": 1,
                             "image_url": "https://test-image.com/normal.jpg",
                             "name": "일반음식점"
                         },
                         {
                             "id": 2,
                             "image_url": "https://test-image.com/ckicken.jpg",
                             "name": "치킨"
                         }
                     ]
                 }
                """);
    }

    @Test
    @DisplayName("특정 상점의 이벤트들을 조회한다.")
    void getShopEvents() {
        eventArticleFixture.할인_이벤트(
            shop,
            LocalDate.now(clock).minusDays(3),
            LocalDate.now(clock).plusDays(3)
        );
        eventArticleFixture.참여_이벤트(
            shop,
            LocalDate.now(clock).minusDays(3),
            LocalDate.now(clock).plusDays(3)
        );

        var response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}/events", shop.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                """);
    }

    @Test
    @DisplayName("이벤트 진행중인 상점의 정보를 조회한다.")
    void getShopWithEvents() {
        eventArticleFixture.할인_이벤트(
            shop,
            LocalDate.now(clock).minusDays(3),
            LocalDate.now(clock).plusDays(3)
        );
        eventArticleFixture.참여_이벤트(
            shop,
            LocalDate.now(clock).minusDays(3),
            LocalDate.now(clock).plusDays(3)
        );

        var response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}", shop.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        Assertions.assertThat(response.jsonPath().getBoolean("is_event")).isTrue();
    }

    @Test
    @DisplayName("이벤트 진행중이지 않은 상점의 정보를 조회한다.")
    void getShopWithoutEvents() {
        eventArticleFixture.할인_이벤트(
            shop,
            LocalDate.now(clock).plusDays(3),
            LocalDate.now(clock).plusDays(5)
        );
        eventArticleFixture.참여_이벤트(
            shop,
            LocalDate.now(clock).minusDays(5),
            LocalDate.now(clock).minusDays(3)
        );

        var response = RestAssured
            .given()
            .when()
            .get("/shops/{shopId}", shop.getId())
            .then()

            .statusCode(HttpStatus.OK.value())
            .extract();

        Assertions.assertThat(response.jsonPath().getBoolean("is_event")).isFalse();
    }

    @Test
    @DisplayName("이벤트 베너 조회")
    void ownerShopDeleteEvent() {
        eventArticleFixture.참여_이벤트(
            shop,
            LocalDate.now(clock),
            LocalDate.now(clock).plusDays(10)
        );
        eventArticleFixture.할인_이벤트(
            shop,
            LocalDate.now(clock).minusDays(10),
            LocalDate.now(clock).minusDays(1)
        );
        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .when()
            .get("/shops/events")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "events": [
                        {
                            "shop_id": 1,
                            "shop_name": "마슬랜 치킨",
                            "event_id": 1,
                            "title": "참여 이벤트",
                            "content": "사장님과 참여해요!!!",
                            "thumbnail_images": [
                                "https://eventimage.com/참여_이벤트.jpg",
                                "https://eventimage.com/참여_이벤트.jpg"
                            ],
                            "start_date": "2024-01-15",
                            "end_date": "2024-01-25"
                        }
                    ]
                }
                """);
    }
}
