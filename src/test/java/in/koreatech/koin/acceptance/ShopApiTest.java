package in.koreatech.koin.acceptance;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.shop.model.Menu;
import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.MenuCategoryMap;
import in.koreatech.koin.domain.shop.model.MenuImage;
import in.koreatech.koin.domain.shop.model.MenuOption;
import in.koreatech.koin.domain.shop.repository.MenuRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class ShopApiTest extends AcceptanceTest {

    @Autowired
    private MenuRepository menuRepository;

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

        MenuCategoryMap menuCategoryMap = MenuCategoryMap.create();

        // when then
        menuOption.setMenu(menu);
        menuImage.setMenu(menu);

        menuCategoryMap.map(menu, menuCategory);

        menuRepository.save(menu);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .get("/shops/{shopId}/menus/{menuId}", menu.getShopId(), menu.getId())
            .then()
            .log().all()
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

        // when then
        menuOption1.setMenu(menu);
        menuOption2.setMenu(menu);
        menuImage1.setMenu(menu);
        menuImage2.setMenu(menu);

        menuCategoryMap.map(menu, menuCategory);

        menuRepository.save(menu);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .get("/shops/{shopId}/menus/{menuId}", menu.getShopId(), menu.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("id")).isEqualTo(menu.getId());

                softly.assertThat(response.body().jsonPath().getLong("shop_id")).isEqualTo(menu.getShopId());
                softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo(menu.getName());
                softly.assertThat(response.body().jsonPath().getBoolean("is_hidden")).isEqualTo(menu.getIsHidden());

                softly.assertThat(response.body().jsonPath().getBoolean("is_single")).isFalse();
                softly.assertThat((Integer)response.body().jsonPath().get("single_price")).isNull();

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
}
