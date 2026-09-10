package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.domain.user.web.model.WebRefreshToken;
import in.koreatech.koin.domain.user.web.repository.WebAuthSessionRedisRepository;

@Testcontainers
class WebAuthSessionRedisRepositoryTest {

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.0.9"))
        .withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;
    private static WebAuthSessionRedisRepository repository;

    @BeforeAll
    static void setUp() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        repository = new WebAuthSessionRedisRepository(redisTemplate, mapper);
    }

    @AfterAll
    static void tearDown() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @Test
    void 동시에_같은_refresh를_갱신하면_하나만_성공한다() throws Exception {
        WebRefreshToken token = WebRefreshToken.create();
        WebAuthSession session = session(token);
        repository.save(session);
        Long previousTtl = redisTemplate.getExpire("webAuthSession:" + session.id());
        WebAuthSession stored = repository.findById(session.id()).orElseThrow();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 8; i++) {
                WebAuthSession next = stored.rotate(token.rotate());
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("동시 갱신 테스트 대기 시간이 초과되었습니다.");
                    }
                    return repository.rotate(stored, next);
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            int succeeded = 0;
            for (Future<Boolean> result : results) {
                if (result.get(10, TimeUnit.SECONDS)) {
                    succeeded++;
                }
            }

            assertThat(succeeded).isEqualTo(1);
            WebAuthSession refreshed = repository.findById(session.id()).orElseThrow();
            assertThat(refreshed.refreshTokenHash()).isNotEqualTo(session.refreshTokenHash());
            assertThat(refreshed.expiresAt()).isEqualTo(session.expiresAt());
            assertThat(redisTemplate.getExpire("webAuthSession:" + session.id())).isPositive().isLessThanOrEqualTo(previousTtl);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void 로그아웃으로_삭제한_세션을_진행중인_재발급이_복구하지_않는다() {
        WebRefreshToken token = WebRefreshToken.create();
        WebAuthSession session = session(token);
        repository.save(session);
        WebAuthSession snapshot = repository.findById(session.id()).orElseThrow();

        assertThat(repository.delete(snapshot)).isTrue();
        assertThat(repository.rotate(snapshot, snapshot.rotate(token.rotate()))).isFalse();
        assertThat(repository.findById(session.id())).isEmpty();
    }

    @Test
    void 이전_스냅샷의_로그아웃이_새로_갱신된_정보를_임의로_삭제하지_않는다() {
        WebRefreshToken token = WebRefreshToken.create();
        WebAuthSession session = session(token);
        repository.save(session);
        WebAuthSession next = session.rotate(token.rotate());

        assertThat(repository.rotate(session, next)).isTrue();
        assertThat(repository.delete(session)).isFalse();
        assertThat(repository.findById(session.id())).contains(next);
    }

    @Test
    void 서버에서_만료된_세션은_다시_갱신할_수_없다() {
        WebRefreshToken token = WebRefreshToken.create();
        WebAuthSession session = session(token);
        repository.save(session);
        redisTemplate.expireAt("webAuthSession:" + session.id(), Instant.now().minusSeconds(1));

        assertThat(repository.findById(session.id())).isEmpty();
        assertThat(repository.rotate(session, session.rotate(token.rotate()))).isFalse();
    }

    private WebAuthSession session(WebRefreshToken token) {
        return new WebAuthSession(token.sessionId(), 1, token.hash(), WebRefreshToken.createSecret(),
            "credential-hash", Instant.now().plusSeconds(120), true);
    }
}
