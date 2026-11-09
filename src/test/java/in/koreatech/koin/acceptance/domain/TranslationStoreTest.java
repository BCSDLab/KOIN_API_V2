package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.time.Duration;
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
import in.koreatech.koin.domain.translation.model.TranslationStatus;
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
    void 동시_요청은_한_작업과_한_선점자만_생성한다() throws Exception {
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
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void 성공한_JSON은_다시_선점하지_않고_그대로_재사용한다() {
        TranslationStore.Claim claim = claim();
        String json = "{\"found_place\":\"학생회관 / Student Union\",\"content\":null}";
        assertThat(store.succeed(claim.translation().id(), claim.ownerToken(), json)).isTrue();

        TranslationStore.Claim cached = claim();
        assertThat(cached.ownerToken()).isNull();
        assertThat(cached.translation().status()).isEqualTo(TranslationStatus.SUCCESS);
        assertThat(cached.translation().translatedJson()).isEqualTo(json);
        assertThat(cached.translation().expiresAt()).isEqualTo(LocalDateTime.now(clock).plusDays(30));
        assertThat(store.fail(claim.translation().id(), claim.ownerToken(),
            TranslationReason.TEMPORARY_ERROR, null)).isFalse();
    }

    @Test
    void 원문과_언어와_모델_정책_및_방_소속이_다르면_별도_결과다() {
        TranslationResource room1 = TranslationResource.message(TranslationType.LOST_ITEM_MESSAGE, 17, 1, "123456789012345678");
        TranslationResource room2 = TranslationResource.message(TranslationType.LOST_ITEM_MESSAGE, 17, 2, "123456789012345678");
        TranslationResource article2 = TranslationResource.message(TranslationType.LOST_ITEM_MESSAGE, 18, 1, "123456789012345678");
        List<String> ids = List.of(
            store.claim(room1, VERSION, SupportedLanguage.ENGLISH, "solar-pro4", "v1").translation().id(),
            store.claim(room2, VERSION, SupportedLanguage.ENGLISH, "solar-pro4", "v1").translation().id(),
            store.claim(article2, VERSION, SupportedLanguage.ENGLISH, "solar-pro4", "v1").translation().id(),
            store.claim(room1, "b".repeat(64), SupportedLanguage.ENGLISH, "solar-pro4", "v1").translation().id(),
            store.claim(room1, VERSION, SupportedLanguage.JAPANESE, "solar-pro4", "v1").translation().id(),
            store.claim(room1, VERSION, SupportedLanguage.ENGLISH, "other-model", "v1").translation().id(),
            store.claim(room1, VERSION, SupportedLanguage.ENGLISH, "solar-pro4", "v2").translation().id()
        );
        assertThat(ids).doesNotHaveDuplicates();
        assertThat(store.findById(ids.get(0)).orElseThrow().expiresAt())
            .isEqualTo(LocalDateTime.now(clock).plusDays(1));
    }

    @Test
    void 실패_재시도는_대기시간을_지키고_외부_대기는_한시간으로_제한한다() {
        TranslationStore.Claim first = claim();
        assertThat(store.fail(first.translation().id(), first.ownerToken(), TranslationReason.RATE_LIMITED,
            Duration.ofHours(24))).isTrue();
        TranslationStore.Claim waiting = claim();
        assertThat(waiting.ownerToken()).isNull();
        assertThat(waiting.translation().status()).isEqualTo(TranslationStatus.FAILED);
        assertThat(waiting.translation().reason()).isEqualTo(TranslationReason.RATE_LIMITED);
        assertThat(waiting.translation().nextAttemptAt()).isEqualTo(LocalDateTime.now(clock).plusHours(1));

        jdbc.update("UPDATE content_translations SET next_attempt_at = ? WHERE id = ?",
            LocalDateTime.now(clock).minusSeconds(1), first.translation().id());
        TranslationStore.Claim retry = claim();
        assertThat(retry.ownerToken()).isNotNull().isNotEqualTo(first.ownerToken());
        assertThat(retry.translation().id()).isEqualTo(first.translation().id());
        assertThat(retry.translation().reason()).isNull();
    }

    @Test
    void 늦은_완료와_실패는_만료된_선점이나_새_선점의_결과를_덮지_못한다() {
        TranslationStore.Claim expired = claim();
        jdbc.update("UPDATE content_translations SET locked_until = ? WHERE id = ?",
            LocalDateTime.now(clock).minusSeconds(1), expired.translation().id());
        assertThat(store.succeed(expired.translation().id(), expired.ownerToken(), "{\"content\":\"old\"}")).isFalse();

        TranslationStore.Claim renewed = claim();
        assertThat(renewed.ownerToken()).isNotNull().isNotEqualTo(expired.ownerToken());
        assertThat(store.fail(expired.translation().id(), expired.ownerToken(),
            TranslationReason.TEMPORARY_ERROR, null)).isFalse();
        assertThat(store.succeed(renewed.translation().id(), renewed.ownerToken(), "{\"content\":\"new\"}")).isTrue();
        assertThat(store.findById(renewed.translation().id()).orElseThrow().translatedJson())
            .isEqualTo("{\"content\":\"new\"}");
    }

    @Test
    void 보관기한이_지난_성공은_새로_선점하고_만료_행만_삭제한다() {
        TranslationStore.Claim original = claim();
        assertThat(store.succeed(original.translation().id(), original.ownerToken(), "{\"content\":\"old\"}")).isTrue();
        expire(original.translation().id());
        TranslationStore.Claim renewed = claim();
        assertThat(renewed.ownerToken()).isNotNull();
        assertThat(renewed.translation().translatedJson()).isNull();
        assertThat(renewed.translation().status()).isEqualTo(TranslationStatus.PENDING);

        StoredTranslation other = store.claim(TranslationResource.article(TranslationType.RECRUITMENT, 9),
            VERSION, SupportedLanguage.ENGLISH, "solar-pro4", "v1").translation();
        expire(renewed.translation().id());
        assertThat(store.deleteExpired()).isEqualTo(1);
        assertThat(store.findById(renewed.translation().id())).isEmpty();
        assertThat(store.findById(other.id())).isPresent();
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

    @Test
    void 외부_재시도_시간이_없어도_기본_대기를_적용한다() {
        TranslationStore.Claim first = claim();
        assertThat(store.fail(first.translation().id(), first.ownerToken(),
            TranslationReason.INVALID_TRANSLATION, Duration.ofSeconds(-1))).isTrue();
        assertThat(store.findById(first.translation().id()).orElseThrow().nextAttemptAt())
            .isEqualTo(LocalDateTime.now(clock).plusSeconds(30));
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

    private void expire(String id) {
        jdbc.update("UPDATE content_translations SET expires_at = ? WHERE id = ?", LocalDateTime.now(clock).minusSeconds(1), id);
    }
}
