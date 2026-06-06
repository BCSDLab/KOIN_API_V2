package in.koreatech.koin.unit.domain.community.keyword.service;

import static in.koreatech.koin.domain.community.keyword.enums.KeywordCategory.KOREATECH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import in.koreatech.koin.common.event.KoreatechArticleKeywordEvent;
import in.koreatech.koin.domain.community.article.model.readmodel.ArticleSummary;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.keyword.dto.KeywordNotificationRequest;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordSuggestRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.keyword.service.ArticleKeywordUserMatcher;
import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import in.koreatech.koin.domain.community.util.KeywordExtractor;
import in.koreatech.koin.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class KeywordServiceTest {

    @InjectMocks
    private KeywordService keywordService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    @Mock
    private ArticleKeywordRepository articleKeywordRepository;

    @Mock
    private ArticleKeywordSuggestRepository articleKeywordSuggestRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeywordExtractor keywordExtractor;

    @Mock
    private ArticleKeywordUserMatcher articleKeywordUserMatcher;

    @Test
    void 공지사항_ID_목록으로_게시글_요약을_조회한다() {
        Set<Integer> articleIds = Set.of(1, 2, 3);
        KeywordNotificationRequest request = new KeywordNotificationRequest(articleIds);
        when(articleRepository.findAllSummariesByIdIn(articleIds)).thenReturn(List.of());

        keywordService.sendKeywordNotification(request);

        verify(articleRepository).findAllSummariesByIdIn(articleIds);
    }

    @Test
    void 게시글_제목에_매칭된_키워드가_없으면_이벤트를_발행하지_않는다() {
        KeywordNotificationRequest request = new KeywordNotificationRequest(Set.of(1));
        ArticleSummary articleSummary = new ArticleSummary(1, 4, "일반 공지입니다");
        when(articleRepository.findAllSummariesByIdIn(request.updateNotification()))
            .thenReturn(List.of(articleSummary));
        when(keywordExtractor.matchKeywords(articleSummary.title(), KOREATECH))
            .thenReturn(List.of());

        keywordService.sendKeywordNotification(request);

        verifyNoInteractions(articleKeywordUserMatcher);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 매칭된_키워드를_가진_사용자가_없으면_이벤트를_발행하지_않는다() {
        KeywordNotificationRequest request = new KeywordNotificationRequest(Set.of(1));
        ArticleSummary articleSummary = new ArticleSummary(1, 4, "수강신청 안내");
        List<String> matchedKeywords = List.of("수강신청");
        when(articleRepository.findAllSummariesByIdIn(request.updateNotification()))
            .thenReturn(List.of(articleSummary));
        when(keywordExtractor.matchKeywords(articleSummary.title(), KOREATECH))
            .thenReturn(matchedKeywords);
        when(articleKeywordUserMatcher.findKeywordsByUserId(KOREATECH, matchedKeywords))
            .thenReturn(Map.of());

        keywordService.sendKeywordNotification(request);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 매칭된_키워드를_가진_사용자가_있으면_키워드_이벤트를_발행한다() {
        KeywordNotificationRequest request = new KeywordNotificationRequest(Set.of(1));
        ArticleSummary articleSummary = new ArticleSummary(1, 4, "수강신청 안내");
        List<String> matchedKeywords = List.of("수강신청");
        Map<Integer, String> keywordByUserId = Map.of(
            10, "수강신청",
            20, "신청"
        );
        when(articleRepository.findAllSummariesByIdIn(request.updateNotification()))
            .thenReturn(List.of(articleSummary));
        when(keywordExtractor.matchKeywords(articleSummary.title(), KOREATECH))
            .thenReturn(matchedKeywords);
        when(articleKeywordUserMatcher.findKeywordsByUserId(KOREATECH, matchedKeywords))
            .thenReturn(keywordByUserId);

        keywordService.sendKeywordNotification(request);

        ArgumentCaptor<KoreatechArticleKeywordEvent> eventCaptor = ArgumentCaptor.forClass(
            KoreatechArticleKeywordEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        KoreatechArticleKeywordEvent event = eventCaptor.getValue();

        assertThat(event.articleId()).isEqualTo(articleSummary.id());
        assertThat(event.boardId()).isEqualTo(articleSummary.boardId());
        assertThat(event.articleTitle()).isEqualTo(articleSummary.title());
        assertThat(event.matchedKeywordUsers().keywordByUserId()).isEqualTo(keywordByUserId);
    }

    @Test
    void 여러_게시글_중_매칭된_사용자가_있는_게시글만_이벤트를_발행한다() {
        KeywordNotificationRequest request = new KeywordNotificationRequest(Set.of(1, 2, 3));
        ArticleSummary unmatchedArticle = new ArticleSummary(1, 4, "일반 공지입니다");
        ArticleSummary noUserArticle = new ArticleSummary(2, 4, "장학금 안내");
        ArticleSummary matchedArticle = new ArticleSummary(3, 4, "수강신청 안내");
        List<String> scholarshipKeywords = List.of("장학금");
        List<String> courseRegistrationKeywords = List.of("수강신청");
        Map<Integer, String> keywordByUserId = Map.of(10, "수강신청");
        when(articleRepository.findAllSummariesByIdIn(request.updateNotification()))
            .thenReturn(List.of(unmatchedArticle, noUserArticle, matchedArticle));
        when(keywordExtractor.matchKeywords(unmatchedArticle.title(), KOREATECH))
            .thenReturn(List.of());
        when(keywordExtractor.matchKeywords(noUserArticle.title(), KOREATECH))
            .thenReturn(scholarshipKeywords);
        when(keywordExtractor.matchKeywords(matchedArticle.title(), KOREATECH))
            .thenReturn(courseRegistrationKeywords);
        when(articleKeywordUserMatcher.findKeywordsByUserId(KOREATECH, scholarshipKeywords))
            .thenReturn(Map.of());
        when(articleKeywordUserMatcher.findKeywordsByUserId(KOREATECH, courseRegistrationKeywords))
            .thenReturn(keywordByUserId);

        keywordService.sendKeywordNotification(request);

        ArgumentCaptor<KoreatechArticleKeywordEvent> eventCaptor = ArgumentCaptor.forClass(
            KoreatechArticleKeywordEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        KoreatechArticleKeywordEvent event = eventCaptor.getValue();

        assertThat(event.articleId()).isEqualTo(matchedArticle.id());
        assertThat(event.boardId()).isEqualTo(matchedArticle.boardId());
        assertThat(event.articleTitle()).isEqualTo(matchedArticle.title());
        assertThat(event.matchedKeywordUsers().keywordByUserId()).isEqualTo(keywordByUserId);
    }
}
