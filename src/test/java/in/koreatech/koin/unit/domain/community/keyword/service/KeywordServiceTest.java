package in.koreatech.koin.unit.domain.community.keyword.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static in.koreatech.koin.domain.community.keyword.enums.KeywordCategory.KOREATECH;
import static in.koreatech.koin.domain.community.keyword.enums.KeywordCategory.LOST_ITEM;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.keyword.dto.ArticleKeywordCreateRequest;
import in.koreatech.koin.domain.community.keyword.dto.KeywordNotificationRequest;
import in.koreatech.koin.domain.community.keyword.exception.KeywordDuplicationException;
import in.koreatech.koin.domain.community.keyword.exception.KeywordLimitExceededException;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordSuggestCache;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordSuggestRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.keyword.repository.UserNotificationStatusRepository;
import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import in.koreatech.koin.domain.community.util.KeywordExtractor;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.unit.fixture.UserFixture;

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
    @DisplayName("키워드 생성은 요청 카테고리 기준으로 제한과 기존 키워드를 조회한다.")
    void createKeyword_usesCategoryForLimitAndKeywordLookup() {
        Integer userId = 1;
        User user = UserFixture.id_설정_코인_유저(userId);
        ArticleKeywordCreateRequest request = new ArticleKeywordCreateRequest("지갑");

        when(articleKeywordUserMapRepository.countByUserIdAndArticleKeywordCategory(userId, LOST_ITEM)).thenReturn(0L);
        when(articleKeywordRepository.findByKeywordAndCategoryIncludingDeleted("지갑", LOST_ITEM.name()))
            .thenReturn(Optional.empty());
        when(articleKeywordRepository.save(any(ArticleKeyword.class))).thenAnswer(invocation -> {
            ArticleKeyword keyword = invocation.getArgument(0);
            ReflectionTestUtils.setField(keyword, "id", 10);
            return keyword;
        });
        when(userRepository.getById(userId)).thenReturn(user);
        when(articleKeywordUserMapRepository.findByArticleKeywordIdAndUserIdIncludingDeleted(10, userId))
            .thenReturn(Optional.empty());
        when(articleKeywordUserMapRepository.save(any(ArticleKeywordUserMap.class))).thenAnswer(invocation -> {
            ArticleKeywordUserMap userMap = invocation.getArgument(0);
            ReflectionTestUtils.setField(userMap, "id", 20);
            return userMap;
        });

        var response = keywordService.createKeyword(userId, request, LOST_ITEM);

        assertThat(response.id()).isEqualTo(20);
        assertThat(response.keyword()).isEqualTo("지갑");
        verify(articleKeywordRepository).save(argThatKeywordCategory(LOST_ITEM));
    }

    @Test
    @DisplayName("같은 사용자와 같은 카테고리의 기존 활성 키워드는 중복으로 처리한다.")
    void createKeyword_withSameCategoryExistingMapping_throwsDuplication() {
        Integer userId = 1;
        ArticleKeyword keyword = ArticleKeyword.builder()
            .keyword("지갑")
            .category(LOST_ITEM)
            .build();
        ReflectionTestUtils.setField(keyword, "id", 10);
        ArticleKeywordUserMap userMap = ArticleKeywordUserMap.builder()
            .articleKeyword(keyword)
            .user(UserFixture.id_설정_코인_유저(userId))
            .build();

        when(articleKeywordUserMapRepository.countByUserIdAndArticleKeywordCategory(userId, LOST_ITEM)).thenReturn(0L);
        when(articleKeywordRepository.findByKeywordAndCategoryIncludingDeleted("지갑", LOST_ITEM.name()))
            .thenReturn(Optional.of(keyword));
        when(articleKeywordUserMapRepository.findByArticleKeywordIdAndUserIdIncludingDeleted(10, userId))
            .thenReturn(Optional.of(userMap));

        assertThatThrownBy(() -> keywordService.createKeyword(userId, new ArticleKeywordCreateRequest("지갑"), LOST_ITEM))
            .isInstanceOf(KeywordDuplicationException.class);
    }

    @Test
    @DisplayName("키워드 등록 제한은 카테고리별로 계산한다.")
    void createKeyword_whenCategoryLimitExceeded_throwsException() {
        Integer userId = 1;
        when(articleKeywordUserMapRepository.countByUserIdAndArticleKeywordCategory(userId, LOST_ITEM)).thenReturn(10L);

        assertThatThrownBy(() -> keywordService.createKeyword(userId, new ArticleKeywordCreateRequest("지갑"), LOST_ITEM))
            .isInstanceOf(KeywordLimitExceededException.class);
    }

    @Test
    @DisplayName("내 키워드 조회는 요청 카테고리 기준으로 조회한다.")
    void getMyKeywords_usesCategory() {
        Integer userId = 1;
        when(articleKeywordUserMapRepository.findAllByUserIdAndArticleKeywordCategory(userId, LOST_ITEM))
            .thenReturn(List.of());

        keywordService.getMyKeywords(userId, LOST_ITEM);

        verify(articleKeywordUserMapRepository).findAllByUserIdAndArticleKeywordCategory(userId, LOST_ITEM);
    }

    @Test
    @DisplayName("추천 키워드는 요청 카테고리의 캐시만 반환한다.")
    void suggestKeywords_filtersByCategory() {
        when(articleKeywordSuggestRepository.findTop15ByOrderByCountDesc())
            .thenReturn(List.of(
                ArticleKeywordSuggestCache.builder()
                    .hotKeywordId(1)
                    .keyword("장학금")
                    .category(KOREATECH)
                    .count(10)
                    .build(),
                ArticleKeywordSuggestCache.builder()
                    .hotKeywordId(2)
                    .keyword("지갑")
                    .category(LOST_ITEM)
                    .count(5)
                    .build()
            ));

        var response = keywordService.suggestKeywords(LOST_ITEM);

        assertThat(response.keywords()).containsExactly("지갑");
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

    private ArticleKeyword argThatKeywordCategory(in.koreatech.koin.domain.community.keyword.enums.KeywordCategory category) {
        return org.mockito.ArgumentMatchers.argThat(keyword -> keyword.getCategory() == category);
    }
}
