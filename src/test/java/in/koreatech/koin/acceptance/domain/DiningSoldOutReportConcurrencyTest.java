package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType.LUNCH;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
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
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.coop.service.CoopService;
import in.koreatech.koin.domain.dining.dto.DiningReportActor;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.repository.DiningReportRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportSequenceRepository;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.dining.service.DiningReportService;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.CoopNotificationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.infrastructure.fcm.FcmSendResponse;
import in.koreatech.koin.infrastructure.s3.client.S3Client;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = {
    "dining.report.bot-token=test-dining-report-bot-token"
})
class DiningSoldOutReportConcurrencyTest extends AcceptanceTest {

    private static final int CONCURRENCY = 2;
    private static final long TIMEOUT_SECONDS = 10;
    private static final String IMAGE_DOMAIN = "https://test.koreatech.in/";
    private static final String IMAGE_KEY =
        "upload/COOP/2024/1/15/e924c7d3-3757-4cd0-961b-e65c1f6cc8ad/soldout.jpg";
    private static final String IMAGE_URL = IMAGE_DOMAIN + IMAGE_KEY;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private DiningAcceptanceFixture diningFixture;

    @Autowired
    private CoopShopAcceptanceFixture coopShopFixture;

    @Autowired
    private DiningRepository diningRepository;

    @Autowired
    private CoopService coopService;

    @Autowired
    private DiningReportService diningReportService;

    @Autowired
    private DiningSoldOutCacheRepository diningSoldOutCacheRepository;

    @Autowired
    private CoopNotificationService coopNotificationService;

    @Autowired
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockBean
    private S3Client s3Client;

    @MockBean
    private FcmClient fcmClient;

    @SpyBean
    private DiningReportRepository reportRepository;

    @SpyBean
    private DiningReportSequenceRepository sequenceRepository;

    private TransactionTemplate transactionTemplate;
    private Integer diningId;
    private Integer firstStudentId;
    private String firstStudentToken;
    private String secondStudentToken;

    @BeforeEach
    void setUp() {
        clear();
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.setTimeout((int)TIMEOUT_SECONDS);
        transactionTemplate.executeWithoutResult(status -> {
            // DBInitializer.clear()가 삭제한 마이그레이션 seed를 테스트 fixture에서만 복원한다.
            jdbcTemplate.update("INSERT INTO dining_soldout_report_sequence (id, last_sequence) VALUES (1, 0)");
            coopShopFixture.현재학기();
            entityManager.flush();
            // JVM 시간대와 무관하게 고정 시각의 품절 알림 경로를 실행한다.
            jdbcTemplate.update("""
                UPDATE coop_opens SET open_time = '00:00', close_time = '23:59' WHERE type = '점심'
                """);
            var department = departmentFixture.컴퓨터공학부();
            var firstStudent = userFixture.준호_학생(department, null).getUser();
            firstStudentId = firstStudent.getId();
            firstStudentToken = userFixture.getToken(firstStudent);
            secondStudentToken = userFixture.getToken(userFixture.성빈_학생(department).getUser());
            diningId = diningFixture.A코너_점심(LocalDate.now(clock.withZone(ZoneId.of("Asia/Seoul")))).getId();
        });
        when(s3Client.getDomainUrlPrefix()).thenReturn(IMAGE_DOMAIN);
        when(s3Client.isCustomDomainUrl(anyString()))
            .thenAnswer(invocation -> invocation.<String>getArgument(0).startsWith(IMAGE_DOMAIN));
        when(s3Client.extractKeyFromUrl(anyString()))
            .thenAnswer(invocation -> invocation.<String>getArgument(0).substring(IMAGE_DOMAIN.length()));
        when(s3Client.doesFileExist(anyString())).thenReturn(true);
        clearInvocations(coopEventListener);
    }

    @AfterEach
    void tearDown() {
        clear();
    }

    @Test
    void 서로_다른_학생과_식단의_빈_잠금조회_후_동시_접수는_모두_성공한다() throws Exception {
        Integer secondDiningId = transactionTemplate.execute(status ->
            diningFixture.B코너_점심(LocalDate.now(clock.withZone(ZoneId.of("Asia/Seoul")))).getId());
        CountDownLatch beforeInsert = new CountDownLatch(CONCURRENCY);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        // Spring Data 인터페이스 spy의 원래 delegate로 실제 INSERT를 실행한다.
        var originalAnswer = mockingDetails(reportRepository).getMockCreationSettings().getDefaultAnswer();
        try {
            doAnswer(invocation -> {
                beforeInsert.countDown();
                if (!beforeInsert.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("두 제보의 빈 잠금조회 완료를 기다리다 시간 초과했습니다.");
                }
                return originalAnswer.answer(invocation);
            }).when(reportRepository).saveAndFlush(any(DiningReport.class));

            // HTTP 밖에 트랜잭션을 열면 create()의 READ_COMMITTED를 덮어쓰므로 직접 요청한다.
            Future<Integer> first = executor.submit(() -> submit(firstStudentToken, diningId));
            Future<Integer> second = executor.submit(() -> submit(secondStudentToken, secondDiningId));
            assertThat(List.of(first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))).containsExactly(201, 201);
            assertThat(beforeInsert.getCount()).isZero();
            List<Map<String, Object>> reports = jdbcTemplate.queryForList("""
                SELECT id, dining_id, reporter_id, status FROM dining_soldout_report ORDER BY id
                """);
            assertThat(reports).hasSize(2);
            assertThat(reports).extracting(row -> ((Number)row.get("dining_id")).intValue())
                .containsExactlyInAnyOrder(diningId, secondDiningId);
            assertThat(reports).extracting(row -> ((Number)row.get("reporter_id")).intValue())
                .contains(firstStudentId).doesNotHaveDuplicates();
            assertThat(reports).extracting(row -> row.get("status")).containsOnly("PENDING");
            assertThat(jdbcTemplate.queryForList("""
                SELECT report_id FROM dining_soldout_report_change
                WHERE event_type = 'CREATED' AND status = 'PENDING' ORDER BY sequence
                """, Integer.class))
                .containsExactlyInAnyOrderElementsOf(reports.stream()
                    .map(row -> ((Number)row.get("id")).intValue()).toList());
        } finally {
            while (beforeInsert.getCount() > 0) {
                beforeInsert.countDown();
            }
            executor.shutdownNow();
            try {
                assertThat(executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            } finally {
                reset(reportRepository);
            }
        }
    }

    @ParameterizedTest(name = "{0}: 서로 다른 식단의 제보 처리와 변경 피드가 모두 커밋된다")
    @CsvSource({
        "approve, APPROVED, MANUAL",
        "reject, REJECTED, MANUAL",
        "coop, REJECTED, COOP_PREPROCESSED"
    })
    void 서로_다른_식단의_처리는_제보와_변경순번_잠금이_엇갈려도_모두_커밋된다(
        String action, String expectedStatus, String expectedProcessingType) throws Exception {
        // Flyway를 끈 Hibernate 스키마에 V12 보조 인덱스와 source FK용 인덱스를 복원한다.
        Map<String, List<String>> indexes = Map.of(
            "idx_dining_report_created", List.of("created_at", "id"),
            "idx_dining_report_pending", List.of("status", "created_at", "id"),
            "idx_dining_report_dining", List.of("dining_id", "status", "id"),
            "idx_dining_report_processing", List.of("processing_id", "id"),
            "fk_dining_report_source", List.of("source_report_id"));
        String indexColumnsSql = """
            SELECT column_name FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'dining_soldout_report'
                AND index_name = ?
            ORDER BY seq_in_index
            """;
        for (var index : indexes.entrySet()) {
            if (jdbcTemplate.queryForList(indexColumnsSql, String.class, index.getKey()).isEmpty()) {
                jdbcTemplate.execute("CREATE INDEX `" + index.getKey() + "` ON dining_soldout_report ("
                    + String.join(", ", index.getValue()) + ")");
            }
            assertThat(jdbcTemplate.queryForList(indexColumnsSql, String.class, index.getKey()))
                .containsExactlyElementsOf(index.getValue());
        }
        // 복합 인덱스가 FK를 충족한 뒤, 운영에 없는 Hibernate dining_id 단일 인덱스를 제거한다.
        List<String> singleDiningIndexes = jdbcTemplate.queryForList("""
            SELECT index_name FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'dining_soldout_report' AND non_unique = 1
            GROUP BY index_name
            HAVING COUNT(*) = 1 AND MIN(column_name) = 'dining_id'
            """, String.class);
        for (String index : singleDiningIndexes) {
            jdbcTemplate.execute("DROP INDEX `" + index.replace("`", "``") + "` ON dining_soldout_report");
        }
        assertThat(jdbcTemplate.queryForList("""
            SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index)
            FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'dining_soldout_report'
            GROUP BY index_name
            """, String.class)).containsExactlyInAnyOrder(
                "id", "reporter_id,dining_id", "created_at,id",
                "status,created_at,id", "dining_id,status,id", "processing_id,id", "source_report_id");

        Integer secondDiningId = transactionTemplate.execute(status ->
            diningFixture.B코너_점심(LocalDate.now(clock.withZone(ZoneId.of("Asia/Seoul")))).getId());
        assertThat(submit(firstStudentToken, diningId)).isEqualTo(201);
        assertThat(submit(secondStudentToken, secondDiningId)).isEqualTo(201);
        Integer firstReportId = jdbcTemplate.queryForObject(
            "SELECT id FROM dining_soldout_report WHERE dining_id = ?", Integer.class, diningId);
        Integer secondReportId = jdbcTemplate.queryForObject(
            "SELECT id FROM dining_soldout_report WHERE dining_id = ?", Integer.class, secondDiningId);
        long sequenceBefore = lastSequence();
        CountDownLatch firstAtSequence = new CountDownLatch(1);
        CountDownLatch secondLockedSequence = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        var originalAnswer = mockingDetails(sequenceRepository).getMockCreationSettings().getDefaultAnswer();
        try {
            doAnswer(invocation -> {
                if (firstAtSequence.getCount() > 0) {
                    // T1의 D1 제보 잠금을 유지한 채 T2가 실제 sequence 잠금을 먼저 얻도록 한다.
                    firstAtSequence.countDown();
                    if (!secondLockedSequence.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("두 번째 식단의 변경순번 잠금을 기다리다 시간 초과했습니다.");
                    }
                    return originalAnswer.answer(invocation);
                }
                var sequence = originalAnswer.answer(invocation);
                secondLockedSequence.countDown();
                return sequence;
            }).when(sequenceRepository).findForUpdate();

            // 외부 트랜잭션 없이 각 서비스의 실제 isolation과 커밋 시점의 UPDATE를 검증한다.
            Future<?> first = executor.submit(() -> process(action, diningId, firstReportId));
            assertThat(firstAtSequence.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            Future<?> second = executor.submit(() -> process(action, secondDiningId, secondReportId));
            first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertThat(secondLockedSequence.getCount()).isZero();

            List<Map<String, Object>> reports = jdbcTemplate.queryForList(
                "SELECT status, processing_type FROM dining_soldout_report ORDER BY id");
            assertThat(reports).hasSize(2);
            assertThat(reports).extracting(row -> row.get("status")).containsOnly(expectedStatus);
            assertThat(reports).extracting(row -> row.get("processing_type")).containsOnly(expectedProcessingType);
            List<Map<String, Object>> changes = jdbcTemplate.queryForList("""
                SELECT sequence, report_id, status, processing_type FROM dining_soldout_report_change
                WHERE event_type = 'PROCESSED' ORDER BY sequence
                """);
            assertThat(changes).extracting(row -> ((Number)row.get("report_id")).intValue())
                .containsExactly(secondReportId, firstReportId);
            assertThat(changes).extracting(row -> ((Number)row.get("sequence")).longValue())
                .containsExactly(sequenceBefore + 1, sequenceBefore + 2);
            assertThat(changes).extracting(row -> row.get("status")).containsOnly(expectedStatus);
            assertThat(changes).extracting(row -> row.get("processing_type")).containsOnly(expectedProcessingType);
            assertThat(lastSequence()).isEqualTo(sequenceBefore + 2);
            List<Map<String, Object>> tasks = jdbcTemplate.queryForList("""
                SELECT report_id, sequence, delivery_id, delivery_state,
                    JSON_UNQUOTE(JSON_EXTRACT(report_snapshot, '$.status')) AS status
                FROM dining_soldout_report_change WHERE event_type = 'PROCESSED' ORDER BY sequence
                """);
            assertThat(tasks).extracting(row -> ((Number)row.get("report_id")).intValue())
                .containsExactly(secondReportId, firstReportId);
            assertThat(tasks).extracting(row -> ((Number)row.get("sequence")).longValue())
                .containsExactly(sequenceBefore + 1, sequenceBefore + 2);
            assertThat(tasks).extracting(row -> row.get("status")).containsOnly(expectedStatus);
            assertThat(tasks).extracting(row -> row.get("delivery_state")).containsOnly("QUEUED");
            assertThat(tasks).allSatisfy(row -> assertThat(row.get("delivery_id")).isNotNull());
        } finally {
            firstAtSequence.countDown();
            secondLockedSequence.countDown();
            executor.shutdownNow();
            try {
                assertThat(executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            } finally {
                reset(sequenceRepository);
            }
        }
    }

    @Test
    void 영양사_품절_롤백은_제보와_변경피드와_캐시를_되돌리고_다음_커밋의_알림을_막지_않는다() throws Exception {
        assertThat(submit(firstStudentToken, diningId)).isEqualTo(201);
        assertThat(submit(secondStudentToken, diningId)).isEqualTo(201);
        String deviceToken = "test-dining-soldout-device-token";
        transactionTemplate.executeWithoutResult(status -> {
            User recipient = entityManager.find(User.class, firstStudentId);
            recipient.permitNotification(deviceToken);
            notificationSubscribeRepository.save(NotificationSubscribe.builder()
                .user(recipient).subscribeType(DINING_SOLD_OUT).build());
            notificationSubscribeRepository.save(NotificationSubscribe.builder()
                .user(recipient).subscribeType(DINING_SOLD_OUT).detailType(LUNCH).build());
        });
        try {
            when(fcmClient.sendMessages(anyList())).thenReturn(List.of(FcmSendResponse.succeeded()));
            doAnswer(invocation -> {
                DiningSoldOutEvent event = invocation.getArgument(0);
                coopNotificationService.sendDiningSoldOutNotifications(event.id(), event.place(), event.diningType());
                return null;
            }).when(coopEventListener).onDiningSoldOutRequest(any(DiningSoldOutEvent.class));

            List<Map<String, Object>> pending = reportRows();
            List<Map<String, Object>> changesBefore = changeRows();
            List<Map<String, Object>> tasksBefore = taskRows();
            long sequenceBefore = lastSequence();
            Dining dining = diningRepository.getById(diningId);
            assertThat(dining.getSoldOut()).isNull();
            assertThat(diningSoldOutCacheRepository.findById(dining.getPlace())).isEmpty();

            transactionTemplate.executeWithoutResult(status -> {
                coopService.changeSoldOut(new SoldOutRequest(diningId, true));
                entityManager.flush();
                assertThat(diningRepository.getById(diningId).getSoldOut()).isNotNull();
                assertThat(reportRows()).extracting(row -> row.get("status")).containsExactly("REJECTED", "REJECTED");
                assertThat(changeRows()).filteredOn(row -> "PROCESSED".equals(row.get("event_type"))).hasSize(2);
                assertThat(lastSequence()).isEqualTo(sequenceBefore + 2);
                status.setRollbackOnly();
            });

            assertThat(diningRepository.getById(diningId).getSoldOut()).isNull();
            assertThat(jdbcTemplate.queryForObject("SELECT sold_out_source FROM dining_menus WHERE id = ?",
                String.class, diningId)).isNull();
            assertThat(reportRows()).isEqualTo(pending);
            assertThat(changeRows()).isEqualTo(changesBefore);
            assertThat(taskRows()).isEqualTo(tasksBefore);
            assertThat(lastSequence()).isEqualTo(sequenceBefore);
            assertThat(diningSoldOutCacheRepository.findById(dining.getPlace())).isEmpty();
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notification", Integer.class)).isZero();
            verify(coopEventListener, never()).onDiningSoldOutRequest(any(DiningSoldOutEvent.class));
            verifyNoInteractions(fcmClient);

            coopService.changeSoldOut(new SoldOutRequest(diningId, true));

            assertThat(diningRepository.getById(diningId).getSoldOut()).isNotNull();
            List<Map<String, Object>> rejected = reportRows();
            assertThat(rejected).hasSize(2);
            assertThat(rejected).extracting(row -> row.get("status")).containsOnly("REJECTED");
            assertThat(rejected).extracting(row -> row.get("processing_type")).containsOnly("COOP_PREPROCESSED");
            assertThat(rejected.get(0).get("processing_id")).isNotNull();
            assertThat(rejected).extracting(row -> row.get("processing_id"))
                .containsOnly(rejected.get(0).get("processing_id"));
            assertThat(changeRows()).hasSize(changesBefore.size() + 2);
            assertThat(lastSequence()).isEqualTo(sequenceBefore + 2);
            assertThat(taskRows()).filteredOn(row -> "PROCESSED".equals(row.get("event_type")))
                .extracting(row -> ((Number)row.get("sequence")).longValue())
                .containsExactly(sequenceBefore + 1, sequenceBefore + 2);
            assertThat(taskRows()).filteredOn(row -> "CREATED".equals(row.get("event_type")))
                .isEqualTo(tasksBefore);
            verify(coopEventListener).onDiningSoldOutRequest(
                new DiningSoldOutEvent(diningId, dining.getPlace(), dining.getType()));
            verify(fcmClient).sendMessages(argThat(requests -> requests.size() == 1
                && deviceToken.equals(requests.get(0).targetDeviceToken())));
            assertThat(diningSoldOutCacheRepository.findById(dining.getPlace())).isPresent();
            assertThat(jdbcTemplate.queryForList("SELECT users_id, app_path FROM notification"))
                .singleElement().satisfies(row -> {
                    assertThat(((Number)row.get("users_id")).intValue()).isEqualTo(firstStudentId);
                    assertThat(row).containsEntry("app_path", "DINING");
                });
            assertThat(jdbcTemplate.queryForObject("SELECT is_push_success FROM notification", Boolean.class)).isTrue();
        } finally {
            reset(coopEventListener, fcmClient);
        }
    }

    private void process(String action, Integer targetDiningId, Integer reportId) {
        DiningReportActor actor = new DiningReportActor("T0123456789", "U0123456789", "홍길동");
        switch (action) {
            case "approve" -> diningReportService.approve(reportId, actor);
            case "reject" -> diningReportService.reject(reportId, actor);
            case "coop" -> coopService.changeSoldOut(new SoldOutRequest(targetDiningId, true));
            default -> throw new IllegalArgumentException("알 수 없는 제보 처리: " + action);
        }
    }

    private int submit(String token, Integer targetDiningId) throws Exception {
        return mockMvc.perform(post("/dinings/{diningId}/soldout-reports", targetDiningId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"image_url\":\"" + IMAGE_URL + "\"}"))
            .andReturn().getResponse().getStatus();
    }

    private List<Map<String, Object>> reportRows() {
        return jdbcTemplate.queryForList("""
            SELECT id, reporter_id, image_url, status, processing_type, HEX(processing_id) AS processing_id,
                source_report_id, processor_workspace_id, processor_user_id, processor_name,
                processed_at, created_at, updated_at
            FROM dining_soldout_report
            WHERE dining_id = ?
            ORDER BY id
            """, diningId);
    }

    private List<Map<String, Object>> changeRows() {
        return jdbcTemplate.queryForList("""
            SELECT c.sequence, c.report_id, c.event_type, c.status, c.processing_type,
                HEX(c.processing_id) AS processing_id, c.occurred_at
            FROM dining_soldout_report_change c
            JOIN dining_soldout_report r ON r.id = c.report_id
            WHERE r.dining_id = ?
            ORDER BY c.sequence
            """, diningId);
    }

    private long lastSequence() {
        return jdbcTemplate.queryForObject(
            "SELECT last_sequence FROM dining_soldout_report_sequence WHERE id = 1", Long.class);
    }

    private List<Map<String, Object>> taskRows() {
        return jdbcTemplate.queryForList("""
            SELECT c.report_id, c.sequence, c.event_type, HEX(c.delivery_id) AS delivery_id,
                c.report_snapshot, c.delivery_state
            FROM dining_soldout_report_change c
            JOIN dining_soldout_report r ON r.id = c.report_id
            WHERE r.dining_id = ? ORDER BY c.sequence
            """, diningId);
    }
}
