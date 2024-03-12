package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.experimental.categories.Categories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsRequest;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.repository.ShopCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.global.auth.JwtProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class OwnerShopApiTest extends AcceptanceTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopCategoryRepository shopCategoryRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private Owner owner;
    private ShopCategory shopCategory;
    private Shop shop;
    private String token;


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

        ShopCategory shopCategoryRequest = ShopCategory.builder()
            .isDeleted(false)
            .name("테스트1")
            .imageUrl("https://test.com/test.jpg")
            .build();
        shopCategory = shopCategoryRepository.save(shopCategoryRequest);
    }

    @Test
    @DisplayName("사장님의 가게 목록을 조회한다.")
    void getOwnerShops() {
        // given
        final int SHOP_COUNT = 2;

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
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("count")).isEqualTo(SHOP_COUNT);
                softly.assertThat(response.body().jsonPath().getList("shops").size()).isEqualTo(SHOP_COUNT);
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
        final int SHOP_COUNT = 2;

        List<Long> categoryIds = new ArrayList<>(Arrays.asList(1L));
        List<String> imageUrls = new ArrayList<>(Arrays.asList(
            "https://test.com/test.jpg",
            "https://test.com/test.jpg",
            "https://test.com/test.jpg"
        ));
        OwnerShopsRequest.InnerOpenRequest open = new OwnerShopsRequest.InnerOpenRequest(
            LocalTime.of(21, 0),
            false,
            "MONDAY",
            LocalTime.of(9, 0)
        );

        OwnerShopsRequest ownerShopsRequest = new OwnerShopsRequest(
            "대전광역시 유성구 대학로 291",
            categoryIds,
            true,
            4000L,
            "테스트 상점2입니다.",
            imageUrls,
            "테스트 상점2",
            open,
            true,
            true,
            "010-1234-5678"
        );

        ExtractableResponse<Response> response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(ownerShopsRequest)
            .when()
            .post("/owner/shops")
            .then()
            .log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        List<Shop> shops =  shopRepository.findAllByOwnerId(owner.getId());
        Shop createdShop = shops.get(1);

        assertSoftly(
            softly -> {
                softly.assertThat(response.statusCode() == 201);
                // softly.assertThat(createdShop.getOwner().);
            }
        );
    }
}
