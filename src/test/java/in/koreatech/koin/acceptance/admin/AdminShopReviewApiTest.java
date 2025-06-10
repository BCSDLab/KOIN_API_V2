package in.koreatech.koin.acceptance.admin;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.DELETED;
import static in.koreatech.koin.domain.shop.model.review.ReportStatus.UNHANDLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.admin.shop.repository.review.AdminShopReviewRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReviewReport;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopNotificationMessageAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopParentCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopReviewAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopReviewReportAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminShopReviewApiTest extends AcceptanceTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private ShopReviewAcceptanceFixture shopReviewFixture;

    @Autowired
    private ShopReviewReportAcceptanceFixture shopReviewReportFixture;

    @Autowired
    private ShopAcceptanceFixture shopFixture;

    @Autowired
    private ShopNotificationMessageAcceptanceFixture shopNotificationMessageFixture;

    @Autowired
    private ShopParentCategoryAcceptanceFixture shopParentCategoryFixture;

    @Autowired
    private ShopCategoryAcceptanceFixture shopCategoryFixture;

    @Autowired
    private AdminShopReviewRepository adminShopReviewRepository;

    private Admin admin;
    private Owner owner_현수;
    private Student student_익명;
    private Department 컴퓨터_공학부;
    private ShopReview 준호_리뷰;
    private Shop shop_마슬랜;
    private String token_admin;
    private ShopCategory shopCategory_치킨;
    private ShopParentCategory shopParentCategory_가게;
    private ShopNotificationMessage notificationMessage_가게;

    @BeforeAll
    void setUp() {
        clear();
        컴퓨터_공학부 = departmentFixture.컴퓨터공학부();
        admin = userFixture.코인_운영자();
        student_익명 = userFixture.익명_학생(컴퓨터_공학부);
        token_admin = userFixture.getToken(admin.getUser());
        owner_현수 = userFixture.현수_사장님();
        shop_마슬랜 = shopFixture.마슬랜(owner_현수, shopCategory_치킨);
        준호_리뷰 = shopReviewFixture.리뷰_4점(student_익명, shop_마슬랜);
        notificationMessage_가게 = shopNotificationMessageFixture.알림메시지_가게();
        shopParentCategory_가게 = shopParentCategoryFixture.상위_카테고리_가게(notificationMessage_가게);
        shopCategory_치킨 = shopCategoryFixture.카테고리_치킨(shopParentCategory_가게);
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
        ShopReviewReport report = shopReviewReportFixture.리뷰_신고(student_익명, 준호_리뷰, UNHANDLED);

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
        assertThat(shopReviewReport.get(0).getReportStatus()).isEqualTo(DELETED);
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
