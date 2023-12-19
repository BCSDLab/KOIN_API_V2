package in.koreatech.koin.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.shop.Menu;
import in.koreatech.koin.domain.shop.MenuCategory;
import in.koreatech.koin.domain.shop.MenuCategoryMap;
import in.koreatech.koin.domain.shop.MenuImage;
import in.koreatech.koin.domain.shop.MenuOption;
import in.koreatech.koin.repository.MenuCategoryMapRepository;
import in.koreatech.koin.repository.MenuCategoryRepository;
import in.koreatech.koin.repository.MenuRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class ShopApiTest extends AcceptanceTest {
    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    @Autowired
    private MenuCategoryMapRepository menuCategoryMapRepository;

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

        menuOption.setMenu(menu);
        menuImage.setMenu(menu);

        MenuCategory menuCategory = MenuCategory.builder()
            .shopId(1L)
            .name("중식")
            .build();

        MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
            .menu(menu)
            .menuCategory(menuCategory)
            .build();

        menuRepository.save(menu);
        menuCategoryRepository.save(menuCategory);

        menuCategoryMapRepository.save(menuCategoryMap);

        // when
        Long menuId = 1L;
        Long shopId = 1L;
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .get("/shops/{shopId}/menus/{menuId}", shopId, menuId)
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();
        // then

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
            .imageUrl("https://test.com/test.jpg")
            .build();

        Menu menu = Menu.builder()
            .shopId(1L)
            .name("짜장면")
            .description("맛있는 짜장면")
            .build();

        menuOption1.setMenu(menu);
        menuOption2.setMenu(menu);
        menuImage1.setMenu(menu);
        menuImage2.setMenu(menu);

        MenuCategory menuCategory = MenuCategory.builder()
            .shopId(1L)
            .name("중식")
            .build();

        MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
            .menu(menu)
            .menuCategory(menuCategory)
            .build();

        // menuOptionRepository.save(menuOption1);
        // menuImageRepository.save(menuImage);

        menuRepository.save(menu);
        menuCategoryRepository.save(menuCategory);

        menuCategoryMapRepository.save(menuCategoryMap);

        // when
        Long menuId = 1L;
        Long shopId = 1L;
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .get("/shops/{shopId}/menus/{menuId}", shopId, menuId)
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();
        // then

    }

}


/*
*
*
*
단일 옵션
```
{
  "id": 170,
  "shop_id": 13,
  "name": "원조김밥",
  "is_hidden": false,
  "is_single": true,
  "single_price": 1500,
  "option_prices": null,
  "description": null,
  "category_ids": [
    144
  ],
  "image_urls": []
}
```
여러 옵션

```
{
  "id": 1502,
  "shop_id": 98,
  "name": "따뜻한족발",
  "is_hidden": false,
  "is_single": false,
  "single_price": null,
  "option_prices": [
    {
      "option": "대",
      "price": 37000
    },
    {
      "option": "소",
      "price": 27000
    },
    {
      "option": "중",
      "price": 32000
    },
    {
      "option": "특",
      "price": 42000
    }
  ],
  "description": null,
  "category_ids": [
    234
  ],
  "image_urls": []
}
```
* */
