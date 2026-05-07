package in.koreatech.koin.unit.domain.community.keyword.service;

import static in.koreatech.koin.domain.community.keyword.enums.KeywordCategory.KOREATECH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.keyword.dto.KeywordNotificationRequest;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordSuggestRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.keyword.repository.UserNotificationStatusRepository;
import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import in.koreatech.koin.domain.community.util.KeywordExtractor;
import in.koreatech.koin.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class KeywordServiceTest {

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
    private UserNotificationStatusRepository userNotificationStatusRepository;

    @Mock
    private KeywordExtractor keywordExtractor;

    @Test
    @DisplayName("중복 게시글 ID 요청은 제거 후 키워드 알림 이벤트를 발행한다.")
    void sendKeywordNotification_withDuplicatedArticleIds_publishesEventsOncePerArticle() {
        KeywordNotificationRequest request = new KeywordNotificationRequest(List.of(10, 10, 11, 11));
        Article article10 = mock(Article.class);
        Article article11 = mock(Article.class);
        when(article10.getId()).thenReturn(10);
        when(article11.getId()).thenReturn(11);
        when(articleRepository.findAllByIdIn(List.of(10, 11))).thenReturn(List.of(article11, article10));

        ArticleKeywordEvent event10 = new ArticleKeywordEvent(
            10,
            null,
            KOREATECH,
            Map.of(1, "A")
        );
        ArticleKeywordEvent event11 = new ArticleKeywordEvent(
            11,
            null,
            KOREATECH,
            Map.of(2, "B")
        );
        when(keywordExtractor.matchKeyword(List.of(article10, article11), null, KOREATECH))
            .thenReturn(List.of(event10, event11));

        keywordService.sendKeywordNotification(request);

        verify(articleRepository).findAllByIdIn(List.of(10, 11));
        verify(keywordExtractor).matchKeyword(List.of(article10, article11), null, KOREATECH);
        verify(eventPublisher).publishEvent(event10);
        verify(eventPublisher).publishEvent(event11);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    @DisplayName("업데이트 알림 대상 게시글이 없으면 아무 작업도 수행하지 않는다.")
    void sendKeywordNotification_withEmptyArticleIds_doesNothing() {
        keywordService.sendKeywordNotification(new KeywordNotificationRequest(List.of()));

        verifyNoInteractions(articleRepository);
        verifyNoInteractions(keywordExtractor);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("발송 이력 저장은 DB upsert를 사용한다.")
    void createNotifiedArticleStatus_usesAtomicUpsert() {
        keywordService.createNotifiedArticleStatus(1, 100);

        verify(userNotificationStatusRepository).upsertLastNotifiedArticleId(1, 100);
    }

    @Test
    @DisplayName("발송 이력 저장은 항상 새로운 트랜잭션에서 수행한다.")
    void createNotifiedArticleStatus_startsNewTransaction() throws NoSuchMethodException {
        Method method = KeywordService.class.getMethod("createNotifiedArticleStatus", Integer.class, Integer.class);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }

    private ArticleKeyword argThatKeywordCategory(
        in.koreatech.koin.domain.community.keyword.enums.KeywordCategory category) {
        return org.mockito.ArgumentMatchers.argThat(keyword -> keyword.getCategory() == category);
    }
}
