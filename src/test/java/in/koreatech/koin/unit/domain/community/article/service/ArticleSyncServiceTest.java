package in.koreatech.koin.unit.domain.community.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.domain.community.article.model.ArticleSearchKeyword;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeywordIpMap;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordIpMapRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import in.koreatech.koin.domain.community.article.repository.redis.ArticleHitRepository;
import in.koreatech.koin.domain.community.article.repository.redis.BusArticleRepository;
import in.koreatech.koin.domain.community.article.repository.redis.HotArticleRepository;
import in.koreatech.koin.domain.community.article.service.ArticleSyncService;

@ExtendWith(MockitoExtension.class)
class ArticleSyncServiceTest {

    private static final String IP_PREFIX = "search:count:ip:";
    private static final String KEYWORD_SET = "popular_keywords";
    private static final String KEYWORD = "장학";

    @Mock private ArticleHitRepository articleHitRepository;
    @Mock private HotArticleRepository hotArticleRepository;
    @Mock private BusArticleRepository busArticleRepository;
    @Mock private ArticleSearchKeywordRepository keywordRepository;
    @Mock private ArticleRepository articleRepository;
    @Mock private Clock clock;
    @Mock private ArticleSearchKeywordIpMapRepository ipMapRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private HashOperations<String, Object, Object> hashOperations;
    @Mock private ZSetOperations<String, Object> zSetOperations;
    @Mock private DataSource dataSource;
    @Mock private Connection connection;

    @InjectMocks
    private ArticleSyncService service;

    private TransactionTemplate transaction;
    private ArticleSearchKeyword keyword;

    @BeforeEach
    void setUp() throws SQLException {
        // Spring drives the commit/rollback lifecycle through a mocked JDBC connection.
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(true);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        keyword = ArticleSearchKeyword.builder().keyword(KEYWORD).weight(1).totalSearch(0).build();
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().when(zSetOperations.reverseRangeWithScores(KEYWORD_SET, 0, -1))
            .thenReturn(Set.of(new DefaultTypedTuple<>(KEYWORD, 1.0)));
        lenient().when(keywordRepository.findByKeyword(KEYWORD)).thenReturn(Optional.of(keyword));
    }

    @Test
    void 기존_IP목록_키에서_첫_IP를_추출하여_저장한다() {
        String clientIp = "2001:db8:1234:5678:9abc:def0:1234:5678";
        String legacyKey = IP_PREFIX + clientIp + ", 10.0.0.12";
        stubCounts(legacyKey, 3);
        when(redisTemplate.keys(IP_PREFIX + "*")).thenReturn(Set.of(legacyKey));

        transaction.executeWithoutResult(status -> service.synchronizeKeywords());

        var saved = ArgumentCaptor.forClass(ArticleSearchKeywordIpMap.class);
        verify(ipMapRepository).save(saved.capture());
        assertThat(saved.getValue().getIpAddress()).isEqualTo(clientIp);
        assertThat(saved.getValue().getSearchCount()).isEqualTo(3);
        verify(redisTemplate).delete(Set.of(legacyKey));
    }

    @Test
    void 일반키와_IP목록키가_같은_IP이면_검색횟수를_합산한다() {
        String clientIp = "203.0.113.20";
        String normalKey = IP_PREFIX + clientIp;
        String legacyKey = normalKey + ", 10.0.0.12";
        stubCounts(normalKey, 3);
        stubCounts(legacyKey, 4);
        when(redisTemplate.keys(IP_PREFIX + "*")).thenReturn(Set.of(normalKey, legacyKey));
        ArticleSearchKeywordIpMap existing = ArticleSearchKeywordIpMap.builder()
            .articleSearchKeyword(keyword).ipAddress(clientIp).searchCount(2).build();
        when(ipMapRepository.findByArticleSearchKeywordAndIpAddress(keyword, clientIp))
            .thenReturn(Optional.of(existing));

        transaction.executeWithoutResult(status -> service.synchronizeKeywords());

        assertThat(existing.getSearchCount()).isEqualTo(9);
        verify(redisTemplate).delete(Set.of(normalKey, legacyKey));
    }

    @Test
    void 해석할_수_없는_IP는_남기고_정상_IP만_동기화한다() {
        String validKey = IP_PREFIX + "203.0.113.20";
        String invalidKey = IP_PREFIX + "unknown, 10.0.0.12";
        stubCounts(validKey, 3);
        lenient().when(hashOperations.entries(invalidKey)).thenReturn(Map.of(KEYWORD, 2));
        when(redisTemplate.keys(IP_PREFIX + "*")).thenReturn(Set.of(invalidKey, validKey));

        transaction.executeWithoutResult(status -> service.synchronizeKeywords());

        var saved = ArgumentCaptor.forClass(ArticleSearchKeywordIpMap.class);
        verify(ipMapRepository).save(saved.capture());
        assertThat(saved.getValue().getIpAddress()).isEqualTo("203.0.113.20");
        verify(redisTemplate).delete(Set.of(validKey));
        verify(redisTemplate, never()).delete(invalidKey);
        verify(redisTemplate).keys(IP_PREFIX + "*");
    }

    @Test
    void 인기검색어가_비어도_남은_IP집계를_저장한다() {
        String ipKey = IP_PREFIX + "203.0.113.20";
        lenient().when(zSetOperations.reverseRangeWithScores(KEYWORD_SET, 0, -1)).thenReturn(Set.of());
        stubCounts(ipKey, 3);
        when(redisTemplate.keys(IP_PREFIX + "*")).thenReturn(Set.of(ipKey));

        transaction.executeWithoutResult(status -> service.synchronizeKeywords());

        verify(ipMapRepository).save(any(ArticleSearchKeywordIpMap.class));
        verify(redisTemplate).delete(Set.of(ipKey));
    }

    @Test
    void 잘못된_IP만_남았으면_원본_키를_삭제하지_않는다() {
        String invalidKey = IP_PREFIX + "not-an-ip";
        lenient().when(zSetOperations.reverseRangeWithScores(KEYWORD_SET, 0, -1)).thenReturn(Set.of());
        when(redisTemplate.keys(IP_PREFIX + "*")).thenReturn(Set.of(invalidKey));

        transaction.executeWithoutResult(status -> service.synchronizeKeywords());

        verify(ipMapRepository, never()).save(any());
        verify(redisTemplate, never()).delete(anyCollection());
        verify(redisTemplate, never()).delete(invalidKey);
    }

    @Test
    void DB커밋이_끝난_뒤에만_Redis_집계를_정리한다() {
        String ipKey = IP_PREFIX + "203.0.113.20";
        stubCounts(ipKey, 3);
        when(redisTemplate.keys(IP_PREFIX + "*")).thenReturn(Set.of(ipKey));

        transaction.executeWithoutResult(status -> {
            service.synchronizeKeywords();
            verifyNoDeletes();
        });

        verify(redisTemplate).delete(Set.of(ipKey));
        verify(redisTemplate).delete(KEYWORD_SET);
    }

    @Test
    void 뒤의_IP를_DB에_저장하다_실패해도_앞의_Redis집계는_남긴다() {
        String firstKey = IP_PREFIX + "203.0.113.20";
        String secondKey = IP_PREFIX + "203.0.113.21";
        stubCounts(firstKey, 3);
        stubCounts(secondKey, 4);
        when(redisTemplate.keys(IP_PREFIX + "*"))
            .thenReturn(new LinkedHashSet<>(List.of(firstKey, secondKey)));
        doAnswer(invocation -> {
            ArticleSearchKeywordIpMap entry = invocation.getArgument(0);
            if (entry.getIpAddress().equals("203.0.113.21")) {
                throw new DataIntegrityViolationException("DB write failed");
            }
            return null;
        }).when(ipMapRepository).save(any());

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> service.synchronizeKeywords()))
            .isInstanceOf(DataIntegrityViolationException.class);

        verifyNoDeletes();
    }

    @Test
    void 동기화_메서드가_끝나도_상위_트랜잭션이_롤백되면_Redis를_보존한다() {
        String ipKey = IP_PREFIX + "203.0.113.20";
        stubCounts(ipKey, 3);
        when(redisTemplate.keys(IP_PREFIX + "*")).thenReturn(Set.of(ipKey));

        transaction.executeWithoutResult(status -> {
            service.synchronizeKeywords();
            status.setRollbackOnly();
        });

        verifyNoDeletes();
    }

    @Test
    void DB커밋_자체가_실패해도_Redis를_보존한다() throws SQLException {
        String ipKey = IP_PREFIX + "203.0.113.20";
        stubCounts(ipKey, 3);
        when(redisTemplate.keys(IP_PREFIX + "*")).thenReturn(Set.of(ipKey));
        doThrow(new SQLException("Commit failed")).when(connection).commit();

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> service.synchronizeKeywords()))
            .hasRootCauseMessage("Commit failed");

        verifyNoDeletes();
    }

    private void stubCounts(String key, int count) {
        when(hashOperations.entries(key)).thenReturn(Map.of(KEYWORD, count));
    }

    private void verifyNoDeletes() {
        verify(redisTemplate, never()).delete(anyString());
        verify(redisTemplate, never()).delete(anyCollection());
    }
}
