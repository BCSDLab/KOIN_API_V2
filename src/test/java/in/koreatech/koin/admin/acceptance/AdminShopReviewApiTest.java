package in.koreatech.koin.admin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.shop.repository.AdminShopReviewRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ShopReviewReport;
import in.koreatech.koin.domain.shop.model.ReportStatus;
import in.koreatech.koin.domain.shop.repository.ShopReviewRepository;
import in.koreatech.koin.domain.shop.repository.ShopReviewReportRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.ShopReviewFixture;
import in.koreatech.koin.fixture.ShopReviewReportFixture;
import in.koreatech.koin.fixture.UserFixture;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
class AdminShopReviewApiTest extends AcceptanceTest {

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
    private AdminShopReviewRepository adminShopReviewRepository;


    private User admin;
    private Owner owner_현수;
    private Student student_익명;
    private ShopReview 준호_리뷰;
    private Shop shop_마슬랜;
    private String token_admin;

    @BeforeEach
    void setUp() {
        admin = userFixture.코인_운영자();
        student_익명 = userFixture.익명_학생();
        token_admin = userFixture.getToken(admin);
        owner_현수 = userFixture.현수_사장님();
        shop_마슬랜 = shopFixture.마슬랜(owner_현수);
        준호_리뷰 = shopReviewFixture.리뷰_4점(student_익명, shop_마슬랜);
    }

    @Test
    @DisplayName("어드민이 모든 리뷰를 조회한다.")
    void findAllReviews() {
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .when()
            .param("page", 1)
            .param("limit", 10)
            .get("/admin/shops/reviews")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                System.out.println(response);;
                softly.assertThat(response.body().jsonPath().getInt("total_count")).isNotNull();
                softly.assertThat(response.body().jsonPath().getInt("current_count")).isNotNull();
                softly.assertThat(response.body().jsonPath().getInt("current_page")).isNotNull();
                softly.assertThat(response.body().jsonPath().getList("reviews").size()).isEqualTo(1);
            }
        );
    }

    @Test
    @DisplayName("어드민이 특정 리뷰의 신고 상태를 변경한다.")
    void modifyReviewReportStatus() {
        ShopReviewReport report = shopReviewReportFixture.리뷰_신고(student_익명, 준호_리뷰, ReportStatus.UNHANDLED);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .contentType("application/json")
            .body("""
                {
                    "report_status": "DELETED"
                }
                """)
            .pathParam("id", report.getId())
            .when()
            .put("/admin/shops/reviews/{id}")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            ShopReview updatedReport = adminShopReviewRepository.findById(report.getReview().getId())
                .orElseThrow();
            List<ShopReviewReport> shopReviewReport = updatedReport.getReports().stream()
                .filter(reviewReport -> reviewReport.getId().equals(report.getId()))
                .toList();

            assertThat(shopReviewReport.get(0).getReportStatus()).isEqualTo(ReportStatus.DELETED);
        });
    }

    @Test
    @DisplayName("어드민이 특정 리뷰를 삭제한다.")
    void deleteReview() {
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token_admin)
            .pathParam("id", 준호_리뷰.getId())
            .when()
            .delete("/admin/shops/reviews/{id}")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            Optional<ShopReview> shopReview = adminShopReviewRepository.findById(준호_리뷰.getId());
            assertThat(shopReview).isPresent();
            assertThat(shopReview.get().isDeleted()).isEqualTo(true);
        });
    }
}
