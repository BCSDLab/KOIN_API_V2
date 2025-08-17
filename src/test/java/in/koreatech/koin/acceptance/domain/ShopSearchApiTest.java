package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.MenuAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.MenuCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopNotificationMessageAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.ShopParentCategoryAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.menu.MenuSearchKeyWord;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.model.shop.ShopParentCategory;
import in.koreatech.koin.domain.shop.repository.menu.MenuSearchKeywordRepository;

class ShopSearchApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private ShopAcceptanceFixture shopFixture;

    @Autowired
    private MenuAcceptanceFixture menuFixture;

    @Autowired
    private MenuCategoryAcceptanceFixture menuCategoryFixture;

    @Autowired
    private MenuSearchKeywordRepository menuSearchKeywordRepository;

    @Autowired
    private ShopCategoryAcceptanceFixture shopCategoryFixture;

    @Autowired
    private ShopNotificationMessageAcceptanceFixture shopNotificationMessageFixture;

    @Autowired
    private ShopParentCategoryAcceptanceFixture shopParentCategoryFixture;

    private Shop 마슬랜;
    private Owner owner;

    private ShopCategory shopCategory_치킨;
    private ShopCategory shopCategory_일반;
    private ShopParentCategory shopParentCategory_가게;
    private ShopNotificationMessage notificationMessage_가게;

    @BeforeAll
    void setUp() {
        clear();
        owner = userFixture.준영_사장님();
        마슬랜 = shopFixture.마슬랜(owner, shopCategory_치킨);
        notificationMessage_가게 = shopNotificationMessageFixture.알림메시지_가게();
        shopParentCategory_가게 = shopParentCategoryFixture.상위_카테고리_가게(notificationMessage_가게);
        shopCategory_치킨 = shopCategoryFixture.카테고리_치킨(shopParentCategory_가게);
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
                get("/shops/search/related/짜")
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
                get("/shops/search/related/마")
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                {
                    "keywords": [
                        {
                            "keyword": "마슬랜 치킨",
                            "shop_ids": [],
                            "shop_id": 1
                        }
                    ]
                }
                """)))
            .andDo(print());
    }
}
