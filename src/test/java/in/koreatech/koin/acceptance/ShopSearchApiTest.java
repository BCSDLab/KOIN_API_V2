package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.DISMISSED;
import static in.koreatech.koin.domain.shop.model.review.ReportStatus.UNHANDLED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.menu.MenuCategory;
import in.koreatech.koin.domain.shop.model.menu.MenuSearchKeyWord;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.menu.MenuSearchKeywordRepository;
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

    @Autowired
    private MenuSearchKeywordRepository menuSearchKeywordRepository;

    private Shop 마슬랜;
    private Owner owner;

    private Student 익명_학생;

    @BeforeAll
    void setUp() {
        clear();
        owner = userFixture.준영_사장님();
        마슬랜 = shopFixture.마슬랜(owner);
        익명_학생 = userFixture.익명_학생();
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("짜장면")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("짜파게티")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("짜장밥")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("떡볶이")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("짬짜면")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("짜장")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("짜게치")
                .build());
    }

    @Test
    void 검색_문자열로_연관_검색어를_조회하면_검색어에_맞는_5개의_연관검색어를_반환한다() throws Exception {
        // 2024-01-15 12:00 월요일 기준
        boolean 신전_떡볶이_영업여부 = true;
        mockMvc.perform(
                        get("/search/related/짜")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "keywords": [
                                            "짜장면",
                                            "짜파게티",
                                            "짜장밥",
                                            "짬짜면",
                                            "짜장"
                                        ]
                        }
                        """, 신전_떡볶이_영업여부)));
    }

    @Test
    void 검색_문자열로_연관_검색어를_조회하면_검색어에_맞는_연관검색어를_반환한다() throws Exception {
        // 2024-01-15 12:00 월요일 기준
        boolean 신전_떡볶이_영업여부 = true;
        mockMvc.perform(
                        get("/search/related/떡")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "keywords": [
                                            "떡볶이"
                                        ]
                        }
                        """, 신전_떡볶이_영업여부)));
    }
}
