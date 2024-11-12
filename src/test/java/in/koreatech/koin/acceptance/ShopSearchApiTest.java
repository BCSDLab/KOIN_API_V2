package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.DISMISSED;
import static in.koreatech.koin.domain.shop.model.review.ReportStatus.UNHANDLED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    private MenuFixture menuFixture;

    @Autowired
    private MenuCategoryFixture menuCategoryFixture;

    @Autowired
    private MenuSearchKeywordRepository menuSearchKeywordRepository;

    private Shop 마슬랜;
    private Owner owner;

    @BeforeAll
    void setUp() {
        clear();
        owner = userFixture.준영_사장님();
        마슬랜 = shopFixture.마슬랜(owner);
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("짜장면")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("마늘치킨")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("짜장밥")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("마늘통구이")
                .build());
        menuSearchKeywordRepository.save(MenuSearchKeyWord.builder()
                .keyword("짜장")
                .build());
        menuFixture.짜장면_단일메뉴(마슬랜, menuCategoryFixture.메인메뉴(마슬랜));
    }

    @Test
    void 검색_문자와_관련된_키워드를_조회한다() throws Exception {
        mockMvc.perform(
                        get("/search/related/짜")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "keywords": [
                                {
                                    "keyword": "짜장",
                                    "shop_ids": [1],
                                    "shop_id": null
                                },
                                {
                                    "keyword": "짜장면",
                                    "shop_ids": [1],
                                    "shop_id": null
                                }
                            ]
                        }
                        """)))
                .andDo(print());
    }

    @Test
    void 검색_문자와_관련된_키워드를_조회한다_상점인_경우에는_상점id도_조회된다() throws Exception {
        mockMvc.perform(
                        get("/search/related/마")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("""
                        {
                            "keywords": [
                                {
                                    "keyword": "마슬랜 치킨",
                                    "shopIds": [],
                                    "shopId": 1
                                }
                            ]
                        }
                        """)))
                .andDo(print());
    }
}
