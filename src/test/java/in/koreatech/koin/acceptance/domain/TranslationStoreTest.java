package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.model.StoredTranslation;
import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.model.TranslationResource;
import in.koreatech.koin.domain.translation.model.TranslationType;
import in.koreatech.koin.domain.translation.service.TranslationStore;

@Transactional(propagation = NOT_SUPPORTED)
@TestPropertySource(properties = {"article.ai-summary.enabled=false", "upstage.api-key=", "translation.cleanup-cron=-"})
class TranslationStoreTest extends AcceptanceTest {
    private static final TranslationResource ARTICLE = TranslationResource.article(TranslationType.LOST_ITEM, 17);
    private static final String VERSION = "a".repeat(64);

    @Autowired
    private TranslationStore store;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    @AfterEach
    void removeTranslations() {
        jdbc.update("DELETE FROM content_translations");
    }

    @Test
    void 동시_요청과_선점_만료_후에도_현재_선점자만_결과를_저장한다() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<TranslationStore.Claim>> futures = List.of(
                executor.submit(() -> claimTogether(ready, start)),
                executor.submit(() -> claimTogether(ready, start))
            );
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            TranslationStore.Claim first = futures.get(0).get(10, TimeUnit.SECONDS);
            TranslationStore.Claim second = futures.get(1).get(10, TimeUnit.SECONDS);
            assertThat(first.translation().id()).isEqualTo(second.translation().id());
            assertThat(List.of(first, second).stream().filter(claim -> claim.ownerToken() != null)).hasSize(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM content_translations", Integer.class)).isEqualTo(1);

            TranslationStore.Claim owner = first.ownerToken() != null ? first : second;
            jdbc.update("UPDATE content_translations SET locked_until = ? WHERE id = ?",
                LocalDateTime.now(clock).minusSeconds(1), owner.translation().id());
            assertThat(store.succeed(owner.translation().id(), owner.ownerToken(), "{\"content\":\"old\"}")).isFalse();
            TranslationStore.Claim renewed = claim();
            assertThat(renewed.translation().id()).isEqualTo(owner.translation().id());
            assertThat(renewed.ownerToken()).isNotNull().isNotEqualTo(owner.ownerToken());
            assertThat(store.succeed(owner.translation().id(), owner.ownerToken(), "{\"content\":\"old\"}")).isFalse();
            assertThat(store.fail(owner.translation().id(), owner.ownerToken(), TranslationReason.TEMPORARY_ERROR, null)).isFalse();
            assertThat(store.succeed(renewed.translation().id(), renewed.ownerToken(), "{\"content\":\"new\"}")).isTrue();
            assertThat(store.findById(owner.translation().id()).orElseThrow().translatedJson())
                .isEqualTo("{\"content\":\"new\"}");
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void 만료_행이_한_배치를_넘어도_모두_삭제하고_보관중인_행은_유지한다() {
        StoredTranslation active = claim().translation();
        LocalDateTime expiredAt = LocalDateTime.now(clock).minusSeconds(1);
        List<Object[]> rows = IntStream.range(0, 1001)
            .mapToObj(index -> new Object[] {UUID.randomUUID().toString(), "expired-" + index, expiredAt, active.id()})
            .toList();
        jdbc.batchUpdate("""
            INSERT INTO content_translations
                (id, cache_key, resource_type, resource_id, parent_id, room_id,
                 source_version, target_language, model, prompt_version, status,
                 expires_at, created_at, updated_at)
            SELECT ?, ?, resource_type, resource_id, parent_id, room_id,
                   source_version, target_language, model, prompt_version, status,
                   ?, created_at, updated_at
            FROM content_translations WHERE id = ?
            """, rows);

        assertThat(store.deleteExpired()).isEqualTo(1001);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM content_translations", Integer.class)).isEqualTo(1);
        assertThat(store.findById(active.id())).contains(active);
        assertThat(store.deleteExpired()).isZero();
    }

    private TranslationStore.Claim claimTogether(CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        if (!start.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("동시 요청 시작 대기 실패");
        }
        return claim();
    }

    private TranslationStore.Claim claim() {
        return store.claim(ARTICLE, VERSION, SupportedLanguage.ENGLISH, "solar-pro4", "v1");
    }
}
