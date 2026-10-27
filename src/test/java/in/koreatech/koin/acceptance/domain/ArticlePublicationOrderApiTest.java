package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.BoardAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleRequest;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.KoinArticle;
import in.koreatech.koin.domain.community.article.model.KoreatechArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;
import in.koreatech.koin.domain.user.model.User;

class ArticlePublicationOrderApiTest extends AcceptanceTest {

    private static final String SEARCH_TERM = "publicationorder";

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardAcceptanceFixture boardFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Board schoolBoard;
    private Board employmentBoard;
    private Board freeBoard;
    private Board lostItemBoard;
    private User author;
    private int portalNumber;
    private boolean createdFulltextIndex;

    @BeforeAll
    void prepareFulltextIndex() {
        clear();
        // application.yml uses ddl-auto=create and disables Flyway. Reproduce V1's title index locally.
        Integer indexCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'new_articles'
              AND index_type = 'FULLTEXT' AND column_name = 'title'
            """, Integer.class);
        if (indexCount == 0) {
            jdbcTemplate.execute("""
                ALTER TABLE new_articles ADD FULLTEXT INDEX idx_publication_order_title (title) WITH PARSER ngram
                """);
            createdFulltextIndex = true;
        }
    }

    @BeforeEach
    void setUp() {
        clear();
        List<Board> boards = boardFixture.코인_게시판_리스트();
        freeBoard = boards.get(0);
        schoolBoard = boards.get(4);
        employmentBoard = boards.get(7);
        assertThat(boards.get(3).getId()).isEqualTo(4);
        assertThat(employmentBoard.getId()).isEqualTo(8);
        // The public default scope excludes the existing lost-item board ID (14).
        for (int id = 10; id <= 14; id++) {
            lostItemBoard = boardRepository.save(Board.builder()
                .name(id == 14 ? "분실물" : "게시판 " + id)
                .articleCount(0)
                .isAnonymous(false)
                .isNotice(false)
                .isDeleted(false)
                .build());
        }
        assertThat(lostItemBoard.getId()).isEqualTo(14);
        author = userFixture.준호_학생(departmentFixture.컴퓨터공학부(), null).getUser();
        portalNumber = 90000;
    }

    @AfterEach
    void removeCommittedSearchData() {
        // InnoDB FULLTEXT only indexes committed rows; search tests explicitly commit their fixtures.
        if (TestTransaction.isActive()) {
            TestTransaction.flagForRollback();
            TestTransaction.end();
        }
        clear();
    }

    @AfterAll
    void removeOwnedFulltextIndex() {
        if (createdFulltextIndex) {
            jdbcTemplate.execute("ALTER TABLE new_articles DROP INDEX idx_publication_order_title");
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {4, 8})
    void listsUsePublicationDateAndRetainOldBackfilledPosts(Integer boardId) throws Exception {
        List<Article> expected = seedBackfillAndFilters(boardId);
        flushAndClear();

        assertPage("/articles", boardId, 1, 10, expected.size(), expected.toArray(Article[]::new));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {4, 8})
    void fulltextSearchUsesPublicationDateAndPreservesScope(Integer boardId) throws Exception {
        List<Article> expected = seedBackfillAndFilters(boardId);
        crawled(employmentBoard, "unrelated vacancy", "2026-11-01", true);
        commitSearchData();

        assertPage("/articles/search", boardId, 1, 10, expected.size(), expected.toArray(Article[]::new));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {4, 8})
    void equalPublicationDatesUseDescendingIdsAcrossPages(Integer boardId) throws Exception {
        Article first = crawled(employmentBoard, SEARCH_TERM + " first", "2026-10-27", true);
        Article second = crawled(employmentBoard, SEARCH_TERM + " second", "2026-10-27", true);
        Article third = crawled(employmentBoard, SEARCH_TERM + " third", "2026-10-27", true);
        Article old = crawled(employmentBoard, SEARCH_TERM + " old", "2025-01-01", true);
        commitSearchData();

        for (String endpoint : List.of("/articles", "/articles/search")) {
            assertPage(endpoint, boardId, 1, 2, 4, third, second);
            assertPage(endpoint, boardId, 2, 2, 4, first, old);
        }
        assertNavigation(boardId, List.of(third, second, first, old));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {4, 8})
    void previousAndNextAreClosestPublicationNeighborsWithinScope(Integer boardId) throws Exception {
        List<Article> expected = seedBackfillAndFilters(boardId);
        flushAndClear();

        assertNavigation(boardId, expected);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {4, 8})
    void missingSourceDateAndNonKoreatechArticlesUseCreatedAt(Integer boardId) throws Exception {
        Article source = crawled(employmentBoard, SEARCH_TERM + " source", "2026-10-27", true);
        Article missingDate = crawled(employmentBoard, SEARCH_TERM + " missing date", null, true);
        Article local = localArticle(employmentBoard, SEARCH_TERM + " local", true);
        Article old = crawled(employmentBoard, SEARCH_TERM + " backfill", "2025-01-01", true);
        setCreatedAt(source, "2026-12-01 12:00:00");
        setCreatedAt(missingDate, "2026-10-26 12:00:00");
        setCreatedAt(local, "2026-10-28 12:00:00");
        commitSearchData();

        for (String endpoint : List.of("/articles", "/articles/search")) {
            assertPage(endpoint, boardId, 1, 10, 4, local, source, missingDate, old);
        }
        assertNavigation(boardId, List.of(local, source, missingDate, old));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {4, 8})
    void storedSourceTimesAreNotTruncatedForOrdering(Integer boardId) throws Exception {
        Article afternoon = crawled(employmentBoard, SEARCH_TERM + " afternoon", "2026-10-27", true);
        Article morning = crawled(employmentBoard, SEARCH_TERM + " morning", "2026-10-27", true);
        // The entity exposes LocalDate, but legacy source timestamps are DATETIME and must remain meaningful.
        entityManager.flush();
        jdbcTemplate.update("UPDATE new_koreatech_articles SET registered_at = ? WHERE article_id = ?",
            "2026-10-27 15:30:00", afternoon.getId());
        jdbcTemplate.update("UPDATE new_koreatech_articles SET registered_at = ? WHERE article_id = ?",
            "2026-10-27 09:00:00", morning.getId());
        commitSearchData();

        for (String endpoint : List.of("/articles", "/articles/search")) {
            assertPage(endpoint, boardId, 1, 10, 2, afternoon, morning)
                .andExpect(jsonPath("$.articles[0].registered_at").value("2026-10-27"))
                .andExpect(jsonPath("$.articles[1].registered_at").value("2026-10-27"));
        }
        assertNavigation(boardId, List.of(afternoon, morning));
        assertThat(jdbcTemplate.queryForObject(
            "SELECT DATE_FORMAT(registered_at, '%Y-%m-%d %H:%i:%s') FROM new_koreatech_articles WHERE article_id = ?",
            String.class, afternoon.getId())).isEqualTo("2026-10-27 15:30:00");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {4, 8})
    void deletedSchoolSourceFallsBackToArticleCreatedAt(Integer boardId) throws Exception {
        Article recent = crawled(employmentBoard, SEARCH_TERM + " recent", "2026-10-27", true);
        Article deletedSource = crawled(employmentBoard, SEARCH_TERM + " deleted source", "2099-01-01", true);
        setCreatedAt(deletedSource, "2026-10-26 12:00:00");
        deletedSource.getKoreatechArticle().delete();
        commitSearchData();

        for (String endpoint : List.of("/articles", "/articles/search")) {
            assertPage(endpoint, boardId, 1, 10, 2, recent, deletedSource);
        }
        assertNavigation(boardId, List.of(recent, deletedSource));
    }

    @Test
    void sharedLostItemSearchAndNavigationKeepTheirBoardAndCreationOrder() throws Exception {
        Article newer = lostItem(SEARCH_TERM + " newer lost item", "2026-10-27 12:00:00");
        Article older = lostItem(SEARCH_TERM + " older lost item", "2025-01-01 12:00:00");
        Article outside = localArticle(freeBoard, SEARCH_TERM + " outside board", false);
        setCreatedAt(outside, "2026-10-26 12:00:00");
        commitSearchData();

        assertPage("/articles/lost-item/search", null, 1, 10, 2, newer, older);
        mockMvc.perform(get("/articles/lost-item/{id}", newer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.prev_id").value(older.getId()))
            .andExpect(jsonPath("$.next_id").isEmpty());
        mockMvc.perform(get("/articles/lost-item/{id}", older.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.prev_id").isEmpty())
            .andExpect(jsonPath("$.next_id").value(newer.getId()));
    }

    private Article lostItem(String title, String createdAt) {
        Article article = Article.createLostItemArticle(new LostItemArticleRequest(
            "LOST", "지갑", "학생회관", LocalDate.of(2024, 1, 10), "분실물 내용", List.of(), null, null),
            lostItemBoard, author);
        article.updateTitle(title);
        articleRepository.save(article);
        setCreatedAt(article, createdAt);
        return article;
    }

    private List<Article> seedBackfillAndFilters(Integer boardId) {
        // A recent school notice is inserted before the old employment crawl receives a larger ID.
        Article school = crawled(schoolBoard, SEARCH_TERM + " school notice", "2026-10-27", true);
        Article employment = crawled(employmentBoard, SEARCH_TERM + " employment", "2026-10-26", true);
        Article tie = crawled(employmentBoard, SEARCH_TERM + " same date", "2026-10-26", true);
        Article old = crawled(employmentBoard, SEARCH_TERM + " old employment", "2025-01-01", true);
        Article ordinary = localArticle(freeBoard, SEARCH_TERM + " ordinary", false);
        setCreatedAt(ordinary, "2026-10-28 12:00:00");
        Article deleted = crawled(employmentBoard, SEARCH_TERM + " deleted", "2026-10-26", true);
        deleted.delete();
        Article lost = Article.createLostItemArticle(new LostItemArticleRequest(
            "LOST", "지갑", "학생회관", LocalDate.of(2026, 10, 26), "분실물 내용", List.of(), null, null),
            lostItemBoard, author);
        lost.updateTitle(SEARCH_TERM + " lost item");
        articleRepository.save(lost);
        setCreatedAt(lost, "2026-10-26 00:00:00");
        assertThat(old.getId()).isGreaterThan(school.getId());

        if (boardId == null) {
            return List.of(ordinary, school, tie, employment, old);
        }
        if (boardId == 4) {
            return List.of(school, tie, employment, old);
        }
        return List.of(tie, employment, old);
    }

    private Article crawled(Board board, String title, String publicationDate, boolean notice) {
        KoreatechArticle source = KoreatechArticle.builder()
            .author("취창업 지원팀")
            .url("https://example.com/" + ++portalNumber)
            .portalNum(portalNumber)
            .portalHit(1)
            .registeredAt(publicationDate == null ? null : LocalDate.parse(publicationDate))
            .isDeleted(false)
            .build();
        Article article = Article.builder()
            .board(board).title(title).content("<p>공지 내용</p>")
            .hit(0).isDeleted(false).isNotice(notice)
            .attachments(new ArrayList<>()).koreatechArticle(source)
            .build();
        source.setArticle(article);
        return articleRepository.save(article);
    }

    private Article localArticle(Board board, String title, boolean notice) {
        KoinArticle source = KoinArticle.builder().user(author).isDeleted(false).build();
        Article article = Article.builder()
            .board(board).title(title).content("<p>게시글 내용</p>")
            .hit(0).isDeleted(false).isNotice(notice)
            .attachments(new ArrayList<>()).koinArticle(source)
            .build();
        source.setArticle(article);
        return articleRepository.save(article);
    }

    private void setCreatedAt(Article article, String createdAt) {
        entityManager.flush();
        jdbcTemplate.update("UPDATE new_articles SET created_at = ? WHERE id = ?", createdAt, article.getId());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void commitSearchData() {
        flushAndClear();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    private ResultActions assertPage(String endpoint, Integer boardId, int page, int limit, int total,
        Article... expected) throws Exception {
        MockHttpServletRequestBuilder request = get(endpoint)
            .param("page", String.valueOf(page)).param("limit", String.valueOf(limit));
        if (boardId != null) {
            request.param("boardId", boardId.toString());
        }
        if (endpoint.endsWith("/search")) {
            request.param("query", SEARCH_TERM);
        }
        return mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articles[*].id").value(contains(Arrays.stream(expected).map(Article::getId).toArray())))
            .andExpect(jsonPath("$.total_count").value(total))
            .andExpect(jsonPath("$.current_count").value(expected.length))
            .andExpect(jsonPath("$.total_page").value((total + limit - 1) / limit))
            .andExpect(jsonPath("$.current_page").value(page));
    }

    private void assertNavigation(Integer boardId, List<Article> descending) throws Exception {
        for (int index = 0; index < descending.size(); index++) {
            // Avoid retaining transient prevId/nextId from another request in the test transaction.
            if (TestTransaction.isActive()) {
                flushAndClear();
            }
            MockHttpServletRequestBuilder request = get("/articles/{id}", descending.get(index).getId());
            if (boardId != null) {
                request.param("boardId", boardId.toString());
            }
            Integer previous = index + 1 < descending.size() ? descending.get(index + 1).getId() : null;
            Integer next = index > 0 ? descending.get(index - 1).getId() : null;
            mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(descending.get(index).getId()))
                .andExpect(jsonPath("$.prev_id").value(previous))
                .andExpect(jsonPath("$.next_id").value(next));
        }
    }
}
