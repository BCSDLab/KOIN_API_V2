package in.koreatech.koin.acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordSuggestCache;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordSuggestRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.fixture.ArticleFixture;
import in.koreatech.koin.fixture.BoardFixture;
import in.koreatech.koin.fixture.KeywordFixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KeywordApiTest extends AcceptanceTest {

    @Autowired
    private ArticleKeywordRepository articleKeywordRepository;

    @Autowired
    private ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    @Autowired
    private ArticleKeywordSuggestRepository articleKeywordSuggestRepository;

    @Autowired
    private KeywordFixture keywordFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private ArticleFixture articleFixture;

    private Student 준호_학생;
    private String token;

    @BeforeAll
    void setup() {
        clear();
        준호_학생 = userFixture.준호_학생();
        token = userFixture.getToken(준호_학생.getUser());
    }

    @Test
    void 알림_키워드_추가() throws Exception {
        mockMvc.perform(
                post("/articles/keyword")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                            "keyword": "장학금"
                        }   
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "id": 1,
                  "keyword": "장학금"
                }
                """));
    }

    @Test
    void 알림_키워드_10개_넘게_추가시_400에러() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(
                get("/articles/keyword")
                    .header("Authorization", "Bearer " + token)
                    .content(String.format("""
                        {
                            "keyword": "keyword%d"
                            }
                        """, i))
                    .contentType(MediaType.APPLICATION_JSON)
            );
        }

        mockMvc.perform(
                get("/articles/keyword")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                            "keyword": "장학금"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 알림_키워드_삭제() throws Exception {
        ArticleKeywordUserMap articleKeywordUserMap = keywordFixture.키워드1("수강 신청", 준호_학생.getUser());

        mockMvc.perform(
                delete("/articles/keyword/{id}", String.valueOf(articleKeywordUserMap.getId()))
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        assertThat(articleKeywordUserMapRepository.findById(articleKeywordUserMap.getId()).isEmpty());
        assertThat(articleKeywordRepository.findById(articleKeywordUserMap.getArticleKeyword().getId()).isEmpty());
    }

    @Test
    void 자신의_알림_키워드_조회() throws Exception {
        ArticleKeywordUserMap articleKeywordUserMap1 = keywordFixture.키워드1("수강신청", 준호_학생.getUser());
        ArticleKeywordUserMap articleKeywordUserMap2 = keywordFixture.키워드1("장학금", 준호_학생.getUser());
        ArticleKeywordUserMap articleKeywordUserMap3 = keywordFixture.키워드1("생활관", 준호_학생.getUser());

        mockMvc.perform(
                get("/articles/keyword/me")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                 {
                    "count": 3,
                    "keywords": [
                        {
                        "id": 1,
                        "keyword": "수강신청"
                        },
                        {
                        "id": 2,
                        "keyword": "장학금"
                        },
                        {
                        "id": 3,
                        "keyword": "생활관"
                        }
                    ]
                }
                """));
    }

    @Test
    void 사용자_아무_것도_추가_안_했을_때_자신의_알림_키워드_조회_빈_리스트_반환() throws Exception {
        mockMvc.perform(
                get("/articles/keyword/me")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                 {
                    "count": 0,
                    "keywords": []
                }
                """));
    }

    @Test
    void 가장_인기_있는_키워드_추천() throws Exception {
        // Redis에 인기 키워드 15개 저장
        List<ArticleKeywordSuggestCache> hotKeywords = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            hotKeywords.add(ArticleKeywordSuggestCache.builder()
                .hotKeywordId(i)
                .keyword("수강신청" + i)
                .build());
        }

        hotKeywords.forEach(keyword -> articleKeywordSuggestRepository.save(keyword));

        mockMvc.perform(
                get("/articles/keyword/suggestions")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "keywords": [
                    "수강신청1", "수강신청2", "수강신청3", "수강신청4", "수강신청5",
                    "수강신청6", "수강신청7", "수강신청8", "수강신청9", "수강신청10",
                    "수강신청11", "수강신청12", "수강신청13", "수강신청14", "수강신청15"
                  ]
                }
                """));
    }

    @Test
    void 새로운_공지사항이_올라오고_해당_키워드를_갖고_있는_사용자가_있을_경우_알림이_발송된다() throws Exception {
        Board board = boardFixture.자유게시판();

        List<Integer> articleIds = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            Article article = articleFixture.자유글_3("수강신청" + i, board, i);
            articleIds.add(article.getId());
        }

        keywordFixture.키워드1("수강신청1", 준호_학생.getUser());

        mockMvc.perform(
                post("/articles/keyword/notification")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                    {
                        "update_notification": %s
                    }
                    """.formatted(articleIds.toString()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        forceVerify(() -> verify(articleKeywordEventListener).onKeywordRequest(any()));
        clear();
        setup();
    }

    @Test
    @Transactional
    void 새로운_공지사항이_올라오고_해당_키워드를_갖고_있는_사용자가_없으면_알림이_발송되지_않는다() throws Exception {

        Board board = boardFixture.자유게시판();

        List<Integer> articleIds = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            Article article = articleFixture.자유글_3("수강신청" + i, board, i);
            articleIds.add(article.getId());
        }

        keywordFixture.키워드1("수강신청6", 준호_학생.getUser());

        mockMvc.perform(
                post("/articles/keyword/notification")
                    .content("""
                    {
                        "update_notification": %s
                    }
                    """.formatted(articleIds.toString()))
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        forceVerify(() -> verify(articleKeywordEventListener, never()).onKeywordRequest(any()));
        clear();
        setup();
    }
}
