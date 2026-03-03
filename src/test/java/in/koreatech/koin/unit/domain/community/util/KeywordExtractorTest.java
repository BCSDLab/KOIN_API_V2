package in.koreatech.koin.unit.domain.community.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.util.KeywordExtractor;

@ExtendWith(MockitoExtension.class)
class KeywordExtractorTest {

    @InjectMocks
    private KeywordExtractor keywordExtractor;

    @Mock
    private ArticleKeywordRepository articleKeywordRepository;

    @Test
    @DisplayName("한 게시글에 여러 키워드가 매칭되면 이벤트는 한 번만 생성된다.")
    void matchKeyword_withMultipleMatchedKeywordsInSingleArticle_createsSingleEvent() {
        Article article = mock(Article.class);
        when(article.getId()).thenReturn(1);
        when(article.getTitle()).thenReturn("ABCD");

        ArticleKeyword keywordA = ArticleKeyword.builder().keyword("A").build();
        ArticleKeyword keywordC = ArticleKeyword.builder().keyword("C").build();

        when(articleKeywordRepository.findAll(any(Pageable.class)))
            .thenReturn(List.of(keywordA, keywordC))
            .thenReturn(List.of());

        List<ArticleKeywordEvent> result = keywordExtractor.matchKeyword(List.of(article), null);

        assertThat(result).hasSize(1);
        ArticleKeywordEvent event = result.get(0);
        assertThat(event.articleId()).isEqualTo(1);
        assertThat(event.authorId()).isNull();
        assertThat(event.matchedKeywords()).containsExactly(keywordA, keywordC);
    }
}
