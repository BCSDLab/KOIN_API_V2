package in.koreatech.koin.unit.domain.community.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.util.KeywordExtractor;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class KeywordExtractorTest {

    @InjectMocks
    private KeywordExtractor keywordExtractor;

    @Mock
    private ArticleKeywordRepository articleKeywordRepository;

    @Mock
    private ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    @Test
    @DisplayName("한 게시글에 여러 키워드가 매칭되면 사용자별 키워드를 병합한 이벤트 한 건만 생성된다.")
    void matchKeyword_withMultipleMatchedKeywordsInSingleArticle_createsSingleEvent() {
        Article article = mock(Article.class);
        when(article.getId()).thenReturn(1);
        when(article.getTitle()).thenReturn("근로장학생 모집");

        User subscriber = UserFixture.id_설정_코인_유저(1);
        ArticleKeyword keywordA = createKeyword(1, "근로", subscriber);
        ArticleKeyword keywordB = createKeyword(2, "근로장학", subscriber);

        when(articleKeywordRepository.findAll())
            .thenReturn(List.of(keywordA, keywordB));
        when(articleKeywordUserMapRepository.findAllByArticleKeywordIdIn(any()))
            .thenReturn(List.of(
                keywordA.getArticleKeywordUserMaps().get(0),
                keywordB.getArticleKeywordUserMaps().get(0)
            ));

        List<ArticleKeywordEvent> result = keywordExtractor.matchKeyword(List.of(article), null);

        assertThat(result).hasSize(1);
        ArticleKeywordEvent event = result.get(0);
        assertThat(event.articleId()).isEqualTo(1);
        assertThat(event.authorId()).isNull();
        assertThat(event.matchedKeywordByUserId()).isEqualTo(Map.of(1, "근로장학"));
    }

    @Test
    @DisplayName("매칭되는 키워드가 없으면 이벤트를 생성하지 않는다.")
    void matchKeyword_whenNoKeywordsMatch_returnsEmptyResult() {
        Article article = mock(Article.class);
        when(article.getId()).thenReturn(1);
        when(article.getTitle()).thenReturn("근로장학생 모집");

        User subscriber = UserFixture.id_설정_코인_유저(1);
        ArticleKeyword keyword = createKeyword(1, "장학금", subscriber);

        when(articleKeywordRepository.findAll())
            .thenReturn(List.of(keyword));
        when(articleKeywordUserMapRepository.findAllByArticleKeywordIdIn(any()))
            .thenReturn(List.of(keyword.getArticleKeywordUserMaps().get(0)));

        List<ArticleKeywordEvent> result = keywordExtractor.matchKeyword(List.of(article), null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 게시글이 각각 다른 키워드에 매칭되면 게시글별 이벤트를 생성한다.")
    void matchKeyword_withMultipleArticles_createsEventPerArticle() {
        Article firstArticle = mock(Article.class);
        when(firstArticle.getId()).thenReturn(1);
        when(firstArticle.getTitle()).thenReturn("근로장학생 모집");

        Article secondArticle = mock(Article.class);
        when(secondArticle.getId()).thenReturn(2);
        when(secondArticle.getTitle()).thenReturn("국가장학금 신청 안내");

        User firstSubscriber = UserFixture.id_설정_코인_유저(1);
        User secondSubscriber = UserFixture.id_설정_코인_유저(2);
        ArticleKeyword firstKeyword = createKeyword(1, "근로", firstSubscriber);
        ArticleKeyword secondKeyword = createKeyword(2, "장학금", secondSubscriber);

        when(articleKeywordRepository.findAll())
            .thenReturn(List.of(firstKeyword, secondKeyword));
        when(articleKeywordUserMapRepository.findAllByArticleKeywordIdIn(any()))
            .thenReturn(List.of(
                firstKeyword.getArticleKeywordUserMaps().get(0),
                secondKeyword.getArticleKeywordUserMaps().get(0)
            ));

        List<ArticleKeywordEvent> result = keywordExtractor.matchKeyword(List.of(firstArticle, secondArticle), null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).articleId()).isEqualTo(1);
        assertThat(result.get(0).matchedKeywordByUserId()).isEqualTo(Map.of(1, "근로"));
        assertThat(result.get(1).articleId()).isEqualTo(2);
        assertThat(result.get(1).matchedKeywordByUserId()).isEqualTo(Map.of(2, "장학금"));
    }

    @Test
    @DisplayName("등록된 키워드가 없으면 빈 결과를 반환한다.")
    void matchKeyword_whenNoKeywordsExist_returnsEmptyResult() {
        Article article = mock(Article.class);
        when(articleKeywordRepository.findAll()).thenReturn(List.of());

        List<ArticleKeywordEvent> result = keywordExtractor.matchKeyword(List.of(article), null);

        assertThat(result).isEmpty();
        verifyNoInteractions(articleKeywordUserMapRepository);
    }

    private ArticleKeyword createKeyword(Integer keywordId, String keyword, User... users) {
        ArticleKeyword articleKeyword = ArticleKeyword.builder()
            .keyword(keyword)
            .build();
        ReflectionTestUtils.setField(articleKeyword, "id", keywordId);
        for (User user : users) {
            ArticleKeywordUserMap userMap = ArticleKeywordUserMap.builder()
                .articleKeyword(articleKeyword)
                .user(user)
                .build();
            articleKeyword.addUserMap(userMap);
        }
        return articleKeyword;
    }
}
