package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.DISMISSED;
import static in.koreatech.koin.domain.shop.model.review.ReportStatus.UNHANDLED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.fixture.EventArticleFixture;
import in.koreatech.koin.fixture.MenuCategoryFixture;
import in.koreatech.koin.fixture.MenuFixture;
import in.koreatech.koin.fixture.ShopCategoryFixture;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.ShopReviewFixture;
import in.koreatech.koin.fixture.ShopReviewReportFixture;
import in.koreatech.koin.fixture.UserFixture;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShopSearchApiTest extends AcceptanceTest {

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ShopFixture shopFixture;

    @Autowired
    private ShopReviewFixture shopReviewFixture;

    @Autowired
    private ShopReviewReportFixture shopReviewReportFixture;

    @Autowired
    private MenuFixture menuFixture;

    @Autowired
    private MenuCategoryFixture menuCategoryFixture;

    @Autowired
    private EventArticleFixture eventArticleFixture;

    @Autowired
    private ShopCategoryFixture shopCategoryFixture;

    private Shop 마슬랜;
    private Owner owner;

    private Student 익명_학생;

    @BeforeAll
    void setUp() {
        clear();
        owner = userFixture.준영_사장님();
        마슬랜 = shopFixture.마슬랜(owner);
        익명_학생 = userFixture.익명_학생();
    }

}
