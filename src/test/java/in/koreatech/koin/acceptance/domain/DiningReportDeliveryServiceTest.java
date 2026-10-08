package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.dining.model.DiningReportChange.EventType.CREATED;
import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryOutcome.FAILED;
import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryOutcome.SUCCEEDED;
import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryStatus.DELIVERED;
import static in.koreatech.koin.domain.dining.model.DiningReportDeliveryStatus.QUEUED;
import static in.koreatech.koin.domain.dining.model.DiningReportStatus.PENDING;
import static in.koreatech.koin.global.code.ApiResponseCode.DINING_REPORT_DELIVERY_CONFLICT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CoopShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DiningAcceptanceFixture;
import in.koreatech.koin.domain.dining.dto.DiningReportActor;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultResponse;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportDeliveryOutcome;
import in.koreatech.koin.domain.dining.repository.DiningReportRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportSequenceRepository;
import in.koreatech.koin.domain.dining.service.DiningReportChangeService;
import in.koreatech.koin.domain.dining.service.DiningReportDeliveryService;
import in.koreatech.koin.domain.dining.service.DiningReportService;
import in.koreatech.koin.global.exception.CustomException;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DiningReportDeliveryServiceTest extends AcceptanceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Instant START = Instant.parse("2024-01-15T03:00:00Z");
    private static final String IMAGE_URL = "https://test.koreatech.in/upload/COOP/soldout.jpg";
    private static final long TIMEOUT_SECONDS = 10;

    @Autowired
    private DiningAcceptanceFixture diningFixture;

    @Autowired
    private CoopShopAcceptanceFixture coopShopFixture;

    @Autowired
    private DiningReportRepository reportRepository;

    @Autowired
    private DiningReportChangeService changeService;

    @Autowired
    private DiningReportService reportService;

    @Autowired
    private DiningReportDeliveryService deliveryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @SpyBean
    private DiningReportSequenceRepository sequenceRepository;

    @MockBean
    private Clock deliveryClock;

    private final AtomicReference<Instant> now = new AtomicReference<>(START);

    @BeforeEach
    void setUp() {
        now.set(START);
        when(deliveryClock.instant()).thenAnswer(invocation -> now.get());
        when(deliveryClock.getZone()).thenReturn(KST);
        when(deliveryClock.withZone(any(ZoneId.class)))
            .thenAnswer(invocation -> Clock.fixed(now.get(), invocation.getArgument(0)));
        clear();
        new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
            jdbcTemplate.update("INSERT INTO dining_soldout_report_sequence (id, last_sequence) VALUES (1, 0)");
            coopShopFixture.현재학기();
        });
    }

    @AfterEach
    void tearDown() {
        clear();
    }

    @Test
    void 변경_스냅샷을_순서대로_배정하고_늦은_성공과_재통보는_후속_작업을_완료하지_않는다() {
        int reportId = createReport();
        var approved = reportService.approve(reportId, new DiningReportActor("T_EXAMPLE", "U_REVIEWER", "담당자"));
        var created = claim();
        assertThat(created.report().status()).isEqualTo(PENDING);
        assertThat(deliveryService.claim()).isEmpty();
        advance(61);
        var completed = result(created, SUCCEEDED);
        assertThat(completed.deliveryState()).isEqualTo(DELIVERED);
        assertThat(result(created, SUCCEEDED)).isEqualTo(completed);
        var processed = claim();
        assertThat(processed.deliveryId()).isNotEqualTo(created.deliveryId());
        assertThat(processed.report()).isEqualTo(approved.report());
        assertThat(result(created, SUCCEEDED)).isEqualTo(completed);
        assertThat(jdbcTemplate.queryForObject("""
            SELECT delivery_state FROM dining_soldout_report_change
            WHERE delivery_id = UNHEX(REPLACE(?, '-', ''))
            """, String.class, processed.deliveryId().toString())).isEqualTo("IN_PROGRESS");
        assertThat(result(processed, SUCCEEDED).deliveryState()).isEqualTo(DELIVERED);
        assertThat(deliveryService.claim()).isEmpty();
    }

    @Test
    void 실패는_5초_뒤_재배정하고_재통보로_대기_시간을_연장하지_않는다() {
        createReport();
        var task = claim();
        assertThat(result(task, FAILED).deliveryState()).isEqualTo(QUEUED);
        advance(4);
        assertThat(result(task, FAILED).deliveryState()).isEqualTo(QUEUED);
        assertThat(deliveryService.claim()).isEmpty();
        advance(1);
        var retry = claim();
        assertSameWork(task, retry);
        assertThat(result(retry, SUCCEEDED).deliveryState()).isEqualTo(DELIVERED);
        assertThat(deliveryService.claim()).isEmpty();
    }

    @Test
    void 배정_60초_만료_즉시_같은_작업을_재배정하고_이전_토큰은_거부한다() {
        createReport();
        var task = claim();
        advance(59);
        assertThat(deliveryService.claim()).isEmpty();
        advance(1);
        var replacement = claim();
        assertSameWork(task, replacement);
        assertThatThrownBy(() -> result(task, SUCCEEDED))
            .isInstanceOfSatisfying(CustomException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(DINING_REPORT_DELIVERY_CONFLICT));
        assertThat(result(replacement, SUCCEEDED).deliveryState()).isEqualTo(DELIVERED);
        assertThat(deliveryService.claim()).isEmpty();
    }

    @Test
    void 실제_순번_잠금에서_경합하는_claim은_하나의_시도만_배정한다() throws Exception {
        createReport();
        var claims = concurrentlyClaim();
        assertThat(claims).extracting(Optional::isPresent).containsExactlyInAnyOrder(true, false);
        var task = claims.stream().flatMap(Optional::stream).findFirst().orElseThrow();
        assertThat(result(task, SUCCEEDED).deliveryState()).isEqualTo(DELIVERED);
        assertThat(deliveryService.claim()).isEmpty();
    }

    @Test
    void 구버전_INSERT는_첫_배정에서_작업_UUID와_스냅샷을_저장하고_완료할_수_있다() {
        createReport();
        result(claim(), SUCCEEDED);
        int legacyReportId = new TransactionTemplate(transactionManager).execute(transaction -> {
            var time = currentTime();
            var dining = diningFixture.B코너_점심(time.toLocalDate());
            var report = reportRepository.saveAndFlush(
                DiningReport.create(dining, null, IMAGE_URL, time));
            long sequence = sequenceRepository.findForUpdate().next();
            jdbcTemplate.update("""
                INSERT INTO dining_soldout_report_change
                    (sequence, report_id, event_type, status, processing_type, processing_id, occurred_at)
                VALUES (?, ?, 'CREATED', 'PENDING', NULL, NULL, ?)
                """, sequence, report.getId(), time);
            return report.getId();
        });
        assertThat(jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM dining_soldout_report_change
            WHERE report_id = ? AND delivery_id IS NULL AND report_snapshot IS NULL
            """, Integer.class, legacyReportId)).isOne();
        var task = claim();
        assertThat(task.report().reportId()).isEqualTo(legacyReportId);
        assertThat(task.report().status()).isEqualTo(PENDING);
        assertThat(jdbcTemplate.queryForObject("""
            SELECT report_snapshot FROM dining_soldout_report_change
            WHERE report_id = ? AND delivery_id = UNHEX(REPLACE(?, '-', ''))
            """, String.class, legacyReportId, task.deliveryId().toString())).isNotBlank();
        assertThat(jdbcTemplate.queryForObject(
            "SELECT last_sequence FROM dining_soldout_report_sequence WHERE id = 1", Long.class)).isEqualTo(2L);
        assertThat(result(task, SUCCEEDED).deliveryState()).isEqualTo(DELIVERED);
        assertThat(deliveryService.claim()).isEmpty();
    }

    private int createReport() {
        return new TransactionTemplate(transactionManager).execute(transaction -> {
            var time = currentTime();
            var dining = diningFixture.A코너_점심(time.toLocalDate());
            var report = reportRepository.saveAndFlush(
                DiningReport.create(dining, null, IMAGE_URL, time));
            changeService.append(List.of(report), CREATED, time);
            return report.getId();
        });
    }

    private DiningReportDeliveryResponse claim() {
        return deliveryService.claim().orElseThrow();
    }

    private DiningReportDeliveryResultResponse result(
        DiningReportDeliveryResponse task, DiningReportDeliveryOutcome outcome
    ) {
        return deliveryService.recordResult(task.deliveryId(),
            new DiningReportDeliveryResultRequest(task.attemptToken(), outcome));
    }

    private void assertSameWork(DiningReportDeliveryResponse original, DiningReportDeliveryResponse next) {
        assertThat(next.deliveryId()).isEqualTo(original.deliveryId());
        assertThat(next.attemptToken()).isNotEqualTo(original.attemptToken());
        assertThat(next.report()).isEqualTo(original.report());
    }

    private LocalDateTime currentTime() {
        return LocalDateTime.ofInstant(now.get(), KST);
    }

    private void advance(long seconds) {
        now.updateAndGet(instant -> instant.plusSeconds(seconds));
    }

    private List<Optional<DiningReportDeliveryResponse>> concurrentlyClaim() throws Exception {
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondAtGate = new CountDownLatch(1);
        CountDownLatch secondLocked = new CountDownLatch(1);
        AtomicInteger acquisitions = new AtomicInteger();
        var executor = Executors.newFixedThreadPool(2);
        var originalAnswer = mockingDetails(sequenceRepository).getMockCreationSettings().getDefaultAnswer();
        try {
            doAnswer(invocation -> {
                if (acquisitions.getAndIncrement() == 0) {
                    var sequence = originalAnswer.answer(invocation);
                    firstLocked.countDown();
                    if (!releaseFirst.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("첫 호출의 순번 잠금 해제 대기 시간 초과");
                    }
                    return sequence;
                }
                secondAtGate.countDown();
                var sequence = originalAnswer.answer(invocation);
                secondLocked.countDown();
                return sequence;
            }).when(sequenceRepository).findForUpdate();

            var first = executor.submit(deliveryService::claim);
            assertThat(firstLocked.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(deliveryService::claim);
            assertThat(secondAtGate.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            // 첫 호출이 실제 DB 잠금을 소유하는 동안 두 번째 잠금 호출은 반환할 수 없다.
            assertThat(secondLocked.await(200, TimeUnit.MILLISECONDS)).isFalse();
            assertThat(second.isDone()).isFalse();
            releaseFirst.countDown();
            var claims = List.of(first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertThat(secondLocked.getCount()).isZero();
            assertThat(acquisitions.get()).isEqualTo(2);
            return claims;
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
            try {
                assertThat(executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            } finally {
                reset(sequenceRepository);
            }
        }
    }
}
