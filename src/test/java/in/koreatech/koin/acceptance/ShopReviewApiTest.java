package in.koreatech.koin.acceptance;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ShopReviewReport;
import in.koreatech.koin.domain.shop.model.ShopReviewReportCategory;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportCategoryRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.ShopReviewFixture;
import in.koreatech.koin.fixture.ShopReviewReportCategoryFixture;
import in.koreatech.koin.fixture.ShopReviewReportFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class ShopReviewApiTest extends AcceptanceTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ShopReviewFixture shopReviewFixture;

    @Autowired
    private ShopReviewReportFixture shopReviewReportFixture;

    @Autowired
    private ShopFixture shopFixture;

    @Autowired
    private ShopReviewReportCategoryFixture shopReviewReportCategoryFixture;

    private ShopReview 준호_학생_리뷰;
    private ShopReview 익명_학생_리뷰;
    private Shop 신전_떡볶이;
    private Owner 현수_사장님;
    private Student 준호_학생;
    private Student 익명_학생;
    private String token_준호;
    private ShopReviewReportCategory 신고_카테고리_1;
    private ShopReviewReportCategory 신고_카테고리_2;
    private ShopReviewReportCategory 신고_카테고리_3;
    private ShopReviewReportCategory 신고_카테고리_4;

    @Autowired
    private ShopReviewRepository shopReviewRepository;

    @Autowired
    private ShopReviewReportRepository shopReviewReportRepository;

    @Autowired
    private ShopReviewReportCategoryRepository shopReviewReportCategoryRepository;

    private final int INITIAL_REVIEW_COUNT = 2;

    @BeforeEach
    void setUp() {
        준호_학생 = userFixture.준호_학생();
        익명_학생 = userFixture.익명_학생();
        현수_사장님 = userFixture.현수_사장님();
        신전_떡볶이 = shopFixture.신전_떡볶이(현수_사장님);
        token_준호 = userFixture.getToken(준호_학생.getUser());
        준호_학생_리뷰 = shopReviewFixture.리뷰(준호_학생, 신전_떡볶이);
        익명_학생_리뷰 = shopReviewFixture.리뷰(익명_학생, 신전_떡볶이);
        신고_카테고리_1 = shopReviewReportCategoryFixture.리뷰_신고_주제에_맞지_않음();
        신고_카테고리_2 = shopReviewReportCategoryFixture.리뷰_신고_스팸();
        신고_카테고리_3 = shopReviewReportCategoryFixture.리뷰_신고_욕설();
        신고_카테고리_4 = shopReviewReportCategoryFixture.리뷰_신고_기타();
    }

    @Test
    @DisplayName("사용자가 리뷰를 등록할 수 있다.")
    void createReview() {
        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준호)
            .body(String.format("""
                {
                  "rating": 4,
                  "content": "정말 맛있어요~!",
                  "image_urls": [
                    "https://static.koreatech.in/example.png"
                  ],
                  "menu_names": [
                    "치킨",
                    "피자"
                  ]
                }
                """))
            .when()
            .pathParam("shopId", 신전_떡볶이.getId())
            .post("/shops/{shopId}/reviews")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            ShopReview shopReview = shopReviewRepository.getById(INITIAL_REVIEW_COUNT + 1);
            assertSoftly(
                softly -> {
                    softly.assertThat(shopReview.getRating()).isEqualTo(4);
                    softly.assertThat(shopReview.getContent()).isEqualTo("정말 맛있어요~!");
                    softly.assertThat(shopReview.getImages().get(0).getImageUrls()).isEqualTo("https://static.koreatech.in/example.png");
                    softly.assertThat(shopReview.getMenus().get(0).getMenuName()).isEqualTo("치킨");
                    softly.assertThat(shopReview.getMenus().get(1).getMenuName()).isEqualTo("피자");
                }
            );
        });
    }

    @Test
    @DisplayName("사용자가 본인의 리뷰를 수정할 수 있다.")
    void modifyReview() {
        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준호)
            .body(String.format("""
                {
                  "rating": 3,
                  "content": "정말 맛있어요!",
                  "image_urls": [
                    "https://static.koreatech.in/example1.png"
                  ],
                  "menu_names": [
                    "피자"
                  ]
                }
                """))
            .when()
            .pathParam("shopId", 신전_떡볶이.getId())
            .pathParam("reviewId", 준호_학생_리뷰.getId())
            .put("/shops/{shopId}/reviews/{reviewId}")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            ShopReview shopReview = shopReviewRepository.getById(준호_학생_리뷰.getId());
            assertSoftly(
                softly -> {
                    softly.assertThat(shopReview.getRating()).isEqualTo(3);
                    softly.assertThat(shopReview.getContent()).isEqualTo("정말 맛있어요!");
                    softly.assertThat(shopReview.getImages().get(0).getImageUrls()).isEqualTo("https://static.koreatech.in/example1.png");
                    softly.assertThat(shopReview.getMenus().size()).isEqualTo(1);
                }
            );
        });
    }

    @Test
    @DisplayName("로그인한 사용자가 리뷰를 조회할 수 있다.")
    void getReview() {
        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준호)
            .when()
            .queryParam("limit", 10)
            .queryParam("page", 1)
            .pathParam("shopId", 준호_학생_리뷰.getShop().getId())
            .get("/shops/{shopId}/reviews")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                   "total_count": 2,
                   "current_count": 2,
                   "total_page": 1,
                   "current_page": 1,
                   "statistics": {
                     "average_rating": 4.0,
                     "ratings": {
                       "1": 0,
                       "2": 0,
                       "3": 0,
                       "4": 2,
                       "5": 0
                     }
                   },
                   "reviews": [
                     {
                       "review_id": %d,
                       "rating": %d,
                       "nick_name": "%s",
                       "content": "%s",
                       "image_urls": [
                         "%s"
                       ],
                       "menu_names": [
                         "%s"
                       ],
                       "is_mine": true,
                       "is_modified": false,
                       "created_at": "2024-01-15"
                     },
                     {
                       "review_id": %d,
                       "rating": %d,
                       "nick_name": "%s",
                       "content": "%s",
                       "image_urls": [
                         "%s"
                       ],
                       "menu_names": [
                         "%s"
                       ],
                       "is_mine": false,
                       "is_modified": false,
                       "created_at": "2024-01-15"
                     }
                   ]
                 }
                """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                익명_학생_리뷰.getId(),
                익명_학생_리뷰.getRating(),
                익명_학생_리뷰.getReviewer().getAnonymousNickname(),
                익명_학생_리뷰.getContent(),
                익명_학생_리뷰.getImages().get(0).getImageUrls(),
                익명_학생_리뷰.getMenus().get(0).getMenuName())
            );
    }

    @Test
    @DisplayName("로그인한 사용자가 자신이 신고한 리뷰를 제외한 모든 리뷰를 조회할 수 있다.")
    void getReviewWithoutReportedReviews() {
        ShopReviewReport shopReviewReport = shopReviewReportFixture.리뷰_신고(준호_학생, 익명_학생_리뷰);
        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준호)
            .when()
            .queryParam("limit", 10)
            .queryParam("page", 1)
            .pathParam("shopId", 신전_떡볶이.getId())
            .get("/shops/{shopId}/reviews")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                   "total_count": 1,
                   "current_count": 1,
                   "total_page": 1,
                   "current_page": 1,
                   "statistics": {
                     "average_rating": 4.0,
                     "ratings": {
                       "1": 0,
                       "2": 0,
                       "3": 0,
                       "4": 1,
                       "5": 0
                     }
                   },
                   "reviews": [
                     {
                       "review_id": %d,
                       "rating": %d,
                       "nick_name": "%s",
                       "content": "%s",
                       "image_urls": [
                         "%s"
                       ],
                       "menu_names": [
                         "%s"
                       ],
                       "is_mine": true,
                       "is_modified": false,
                       "created_at": "2024-01-15"
                     }
                   ]
                 }
                """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName())
            );
    }

    @Test
    @DisplayName("비회원이 리뷰를 조회할 수 있다.")
    void getReviewByUnauthenticatedUser() {
        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .when()
            .queryParam("limit", 10)
            .queryParam("page", 1)
            .pathParam("shopId", 신전_떡볶이.getId())
            .get("/shops/{shopId}/reviews")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                   "total_count": 2,
                   "current_count": 2,
                   "total_page": 1,
                   "current_page": 1,
                   "statistics": {
                     "average_rating": 4.0,
                     "ratings": {
                       "1": 0,
                       "2": 0,
                       "3": 0,
                       "4": 2,
                       "5": 0
                     }
                   },
                   "reviews": [
                     {
                       "review_id": %d,
                       "rating": %d,
                       "nick_name": "%s",
                       "content": "%s",
                       "image_urls": [
                         "%s"
                       ],
                       "menu_names": [
                         "%s"
                       ],
                       "is_mine": false,
                       "is_modified": false,
                       "created_at": "2024-01-15"
                     },
                     {
                       "review_id": %d,
                       "rating": %d,
                       "nick_name": "%s",
                       "content": "%s",
                       "image_urls": [
                         "%s"
                       ],
                       "menu_names": [
                         "%s"
                       ],
                       "is_mine": false,
                       "is_modified": false,
                       "created_at": "2024-01-15"
                     }
                   ]
                 }
                """,
                준호_학생_리뷰.getId(),
                준호_학생_리뷰.getRating(),
                준호_학생_리뷰.getReviewer().getUser().getNickname(),
                준호_학생_리뷰.getContent(),
                준호_학생_리뷰.getImages().get(0).getImageUrls(),
                준호_학생_리뷰.getMenus().get(0).getMenuName(),
                익명_학생_리뷰.getId(),
                익명_학생_리뷰.getRating(),
                익명_학생_리뷰.getReviewer().getAnonymousNickname(),
                익명_학생_리뷰.getContent(),
                익명_학생_리뷰.getImages().get(0).getImageUrls(),
                익명_학생_리뷰.getMenus().get(0).getMenuName())
            );
    }

    @Test
    @DisplayName("리뷰 신고 카테고리를 조회할 수 있다.")
    void getReviewReportCategories() {
        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .when()
            .get("/shops/reviews/reports/categories")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                  "count": 4,
                  "categories": [
                    {
                      "name": "%s",
                      "detail": "%s"
                    },
                    {
                      "name": "%s",
                      "detail": "%s"
                    },
                    {
                      "name": "%s",
                      "detail": "%s"
                    },
                    {
                      "name": "%s",
                      "detail": "%s"
                    }
                  ]
                }
                """,
                신고_카테고리_1.getName(),
                신고_카테고리_1.getDetail(),
                신고_카테고리_2.getName(),
                신고_카테고리_2.getDetail(),
                신고_카테고리_3.getName(),
                신고_카테고리_3.getDetail(),
                신고_카테고리_4.getName(),
                신고_카테고리_4.getDetail())
            );
    }

    @Test
    @DisplayName("특정 리뷰를 신고한다.")
    void reportReview() {

        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준호)
            .body("""
                {
                    "title": "기타",
                    "content": "적절치 못한 리뷰인 것 같습니다."
                }
                """)
            .when()
            .pathParam("shopId", 준호_학생_리뷰.getShop().getId())
            .pathParam("reviewId", 준호_학생_리뷰.getId())
            .post("/shops/{shopId}/reviews/{reviewId}/reports")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            Optional<ShopReviewReport> shopReviewReport = shopReviewReportRepository.findById(1);
            assertSoftly(
                softly -> {
                    softly.assertThat(shopReviewReport.isPresent()).isTrue();
                    softly.assertThat(shopReviewReport.get().getTitle()).isEqualTo("기타");
                    softly.assertThat(shopReviewReport.get().getContent()).isEqualTo("적절치 못한 리뷰인 것 같습니다.");
                }
            );
        });
    }

    @Test
    @DisplayName("학생이 자신이 작성한 리뷰를 삭제한다.")
    void deleteMyReview() {
        var response = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token_준호)
            .when()
            .pathParam("shopId", 준호_학생_리뷰.getShop().getId())
            .pathParam("reviewId", 준호_학생_리뷰.getId())
            .delete("/shops/{shopId}/reviews/{reviewId}")
            .then()
            .log().all()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            Optional<ShopReviewReport> shopReviewReport = shopReviewReportRepository.findById(1);
            assertSoftly(
                softly -> {
                    softly.assertThat(shopReviewReport.isPresent()).isFalse();
                }
            );
        });
    }
}
