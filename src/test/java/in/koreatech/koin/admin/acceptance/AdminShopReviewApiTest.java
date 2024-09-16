package in.koreatech.koin.admin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.shop.repository.AdminShopReviewRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.ReportStatus;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopReview;
import in.koreatech.koin.domain.shop.model.ShopReviewReport;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.ShopReviewFixture;
import in.koreatech.koin.fixture.ShopReviewReportFixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @BeforeAll
    void setUp() {
        clear();
        admin = userFixture.코인_운영자();
        student_익명 = userFixture.익명_학생();
        token_admin = userFixture.getToken(admin);
        owner_현수 = userFixture.현수_사장님();
        shop_마슬랜 = shopFixture.마슬랜(owner_현수);
        준호_리뷰 = shopReviewFixture.리뷰_4점(student_익명, shop_마슬랜);
    }

    @Test
    void 어드민이_모든_리뷰를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/shops/reviews")
                    .header("Authorization", "Bearer " + token_admin)
                    .param("page", "1")
                    .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").exists())
            .andExpect(jsonPath("$.current_count").exists())
            .andExpect(jsonPath("$.current_page").exists())
            .andExpect(jsonPath("$.reviews").isArray())
            .andExpect(jsonPath("$.reviews.length()").value(1));
    }

    @Test
    void 어드민이_특정_리뷰의_신고_상태를_변경한다() throws Exception {
        ShopReviewReport report = shopReviewReportFixture.리뷰_신고(student_익명, 준호_리뷰, ReportStatus.UNHANDLED);

        mockMvc.perform(
                put("/admin/shops/reviews/{id}", report.getId())
                    .header("Authorization", "Bearer " + token_admin)
                    .contentType("application/json")
                    .content("""
                        {
                            "report_status": "DELETED"
                        }
                        """))
            .andExpect(status().isOk());

        ShopReview updatedReport = adminShopReviewRepository
            .findById(report.getReview().getId())
            .orElseThrow();
        List<ShopReviewReport> shopReviewReport = updatedReport.getReports().stream()
            .filter(reviewReport -> reviewReport.getId().equals(report.getId()))
            .toList();
        assertThat(shopReviewReport.get(0).getReportStatus()).isEqualTo(ReportStatus.DELETED);
    }

    @Test
    void 어드민이_특정_리뷰를_삭제한다() throws Exception {
        mockMvc.perform(
                delete("/admin/shops/reviews/{id}", 준호_리뷰.getId())
                    .header("Authorization", "Bearer " + token_admin))
            .andExpect(status().isNoContent());

        Optional<ShopReview> shopReview = adminShopReviewRepository.findById(준호_리뷰.getId());
        assertThat(shopReview).isPresent();
        assertThat(shopReview.get().isDeleted()).isEqualTo(true);
    }
}
