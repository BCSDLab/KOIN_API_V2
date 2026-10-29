package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType.LUNCH;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CoopShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DiningAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.common.event.DiningSoldOutEvent;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.notification.eventlistener.CoopEventListener;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.CoopNotificationService;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.infrastructure.fcm.FcmSendRequest;
import in.koreatech.koin.infrastructure.fcm.FcmSendResponse;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(DiningSoldOutNotificationConcurrencyTest.NotificationLockConfig.class)
class DiningSoldOutNotificationConcurrencyTest extends AcceptanceTest {

    private static final long TIMEOUT_SECONDS = 15;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private DiningAcceptanceFixture diningFixture;

    @Autowired
    private CoopShopAcceptanceFixture coopShopFixture;

    @Autowired
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Autowired
    private DiningSoldOutCacheRepository cacheRepository;

    @Autowired
    private CoopNotificationService notificationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private RedissonClient redissonClient;

    @MockBean
    private FcmClient fcmClient;

    private Integer diningId;
    private Integer otherDiningId;
    private String coopToken;

    @BeforeEach
    void setUp() {
        clear();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> {
            coopShopFixture.현재학기();
            entityManager.flush();
            jdbcTemplate.update("""
                UPDATE coop_opens SET open_time = '00:00', close_time = '23:59' WHERE type = '점심'
                """);
            coopToken = userFixture.getToken(userFixture.준기_영양사().getUser());
            var recipient = userFixture.준호_학생(departmentFixture.컴퓨터공학부(), null).getUser();
            recipient.permitNotification("dining-soldout-concurrency-device-token");
            notificationSubscribeRepository.save(NotificationSubscribe.builder()
                .user(recipient).subscribeType(DINING_SOLD_OUT).build());
            notificationSubscribeRepository.save(NotificationSubscribe.builder()
                .user(recipient).subscribeType(DINING_SOLD_OUT).detailType(LUNCH).build());
            diningId = diningFixture.A코너_점심(LocalDate.now(clock.withZone(ZoneId.of("Asia/Seoul")))).getId();
            otherDiningId = diningFixture.B코너_점심(LocalDate.now(clock.withZone(ZoneId.of("Asia/Seoul")))).getId();
        });
    }

    @AfterEach
    void tearDown() {
        clear();
    }

    @Test
    void FCM_6초_지연에도_같은_장소는_한번만_전송하고_다른_장소는_독립적으로_진행한다() throws Exception {
        CountDownLatch firstSendEntered = new CountDownLatch(1);
        CountDownLatch releaseFirstSend = new CountDownLatch(1);
        CountDownLatch committedEvents = new CountDownLatch(2);
        CountDownLatch completedNotifications = new CountDownLatch(3);
        AtomicLong firstSendThread = new AtomicLong();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CoopEventListener realListener = new CoopEventListener(notificationService, redissonClient);
        RLock placeLock = redissonClient.getLock("lock:coop-dining-soldout-notification:A코너");
        try {
            // 공통 test profile의 listener mock을 실제 listener 메서드로 연결한다.
            // 실제 HTTP 트랜잭션의 AFTER_COMMIT과 RedisHash 캐시는 그대로 사용한다.
            doAnswer(invocation -> {
                DiningSoldOutEvent event = invocation.getArgument(0);
                if (event.place().equals("A코너")) {
                    committedEvents.countDown();
                }
                realListener.onDiningSoldOutRequest(event);
                completedNotifications.countDown();
                return null;
            }).when(coopEventListener).onDiningSoldOutRequest(any(DiningSoldOutEvent.class));
            doAnswer(invocation -> {
                List<FcmSendRequest> requests = invocation.getArgument(0);
                if (requests.get(0).title().startsWith("A코너")
                    && firstSendThread.compareAndSet(0, Thread.currentThread().getId())) {
                    firstSendEntered.countDown();
                    if (!releaseFirstSend.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("첫 FCM 전송 해제 대기 시간이 초과되었습니다.");
                    }
                }
                return List.of(FcmSendResponse.succeeded());
            }).when(fcmClient).sendMessages(anyList());

            Future<Integer> first = executor.submit(() -> changeSoldOut(diningId, true));
            assertThat(firstSendEntered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            assertThat(jdbcTemplate.queryForObject("SELECT sold_out_source FROM dining_menus WHERE id = ?",
                String.class, diningId)).isEqualTo("COOP");
            assertThat(cacheRepository.findById("A코너")).isEmpty();

            Future<Integer> second = executor.submit(() -> {
                assertThat(changeSoldOut(diningId, false)).isEqualTo(200);
                return changeSoldOut(diningId, true);
            });
            assertThat(committedEvents.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            Future<Integer> other = executor.submit(() -> changeSoldOut(otherDiningId, true));
            // A의 FCM을 6초 이상 붙잡아 기본 5초 lease를 잘못 지정하면 실패한다.
            assertThatThrownBy(() -> second.get(6, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);
            assertThat(placeLock.isHeldByThread(firstSendThread.get())).isTrue();
            assertThat(cacheRepository.findById("A코너")).isEmpty();
            // 다른 장소는 A의 락 해제 전에 HTTP, 알림, 캐시 저장을 모두 완료해야 한다.
            assertThat(other.isDone()).isTrue();
            assertThat(other.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo(200);
            assertThat(cacheRepository.findById("B코너")).isPresent();
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notification", Integer.class)).isEqualTo(1);
            releaseFirstSend.countDown();
            assertThat(first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo(200);
            assertThat(second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo(200);
            // AFTER_COMMIT 예외가 HTTP 200과 함께 로그로만 남아도 성공으로 오인하지 않는다.
            assertThat(completedNotifications.getCount()).isZero();
            verify(coopEventListener, times(3)).onDiningSoldOutRequest(any(DiningSoldOutEvent.class));
            verify(fcmClient, times(2)).sendMessages(anyList());
            verify(fcmClient).sendMessages(argThat(requests -> requests.size() == 1
                && requests.get(0).title().startsWith("A코너")));
            verify(fcmClient).sendMessages(argThat(requests -> requests.size() == 1
                && requests.get(0).title().startsWith("B코너")));
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notification", Integer.class)).isEqualTo(2);
            assertThat(cacheRepository.findById("A코너")).isPresent();
            assertThat(redisTemplate.type("diningSoldOut:A코너")).isEqualTo(DataType.HASH);
            assertThat(redisTemplate.getExpire("diningSoldOut:A코너", TimeUnit.SECONDS)).isBetween(7190L, 7200L);
            assertThat(redisTemplate.type("diningSoldOut:B코너")).isEqualTo(DataType.HASH);
            assertThat(redisTemplate.getExpire("diningSoldOut:B코너", TimeUnit.SECONDS)).isBetween(7190L, 7200L);
            assertThat(placeLock.isLocked()).isFalse();
            assertThat(redissonClient.getLock("lock:coop-dining-soldout-notification:B코너").isLocked()).isFalse();
        } finally {
            releaseFirstSend.countDown();
            executor.shutdownNow();
            try {
                assertThat(executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            } finally {
                reset(coopEventListener, fcmClient);
            }
        }
    }

    private int changeSoldOut(Integer targetDiningId, boolean soldOut) throws Exception {
        return mockMvc.perform(patch("/coop/dining/soldout")
                .header("Authorization", "Bearer " + coopToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menu_id\":%d,\"sold_out\":%s}".formatted(targetDiningId, soldOut)))
            .andReturn().getResponse().getStatus();
    }

    @TestConfiguration
    static class NotificationLockConfig {

        @Bean(destroyMethod = "shutdown")
        RedissonClient notificationRedissonClient(@Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port) {
            Config config = new Config();
            // 테스트에서 watchdog 갱신도 6초 지연 안에 검증한다. 운영 설정은 변경하지 않는다.
            config.setLockWatchdogTimeout(3_000);
            config.useSingleServer().setAddress("redis://" + host + ":" + port);
            return Redisson.create(config);
        }
    }
}
