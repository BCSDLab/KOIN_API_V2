package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CoopShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DiningAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.dining.repository.DiningReportSequenceRepository;
import in.koreatech.koin.infrastructure.s3.client.S3Client;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestPropertySource(properties = {
    "dining.report.bot-token=test-dining-report-bot-token",
    "dining.report.delivery.workspace-id=T_EXAMPLE",
    "dining.report.delivery.channel-id=C_EXAMPLE"
})
class DiningReportDeliveryApiTest extends AcceptanceTest {

    private static final String BOT_PATH = "/internal/dining/soldout-reports";
    private static final String BOT_HEADER = "X-Koin-Service-Token";
    private static final String BOT_TOKEN = "test-dining-report-bot-token";
    private static final String IMAGE_DOMAIN = "https://test.koreatech.in/";
    private static final String IMAGE_URL = IMAGE_DOMAIN
        + "upload/COOP/2024/1/15/e924c7d3-3757-4cd0-961b-e65c1f6cc8ad/soldout.jpg";
    private static final String MESSAGE_TS = "1791257400.000100";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Instant START = Instant.parse("2024-01-15T03:00:00Z");
    private static final long TIMEOUT_SECONDS = 10;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private DiningAcceptanceFixture diningFixture;

    @Autowired
    private CoopShopAcceptanceFixture coopShopFixture;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private S3Client s3Client;

    @MockBean
    private Clock deliveryClock;

    @SpyBean
    private DiningReportSequenceRepository sequenceRepository;

    private final AtomicReference<Instant> now = new AtomicReference<>(START);
    private String studentToken;
    private Integer diningId;
    private Integer otherDiningId;

    @BeforeEach
    void setUp() {
        now.set(START);
        when(deliveryClock.instant()).thenAnswer(invocation -> now.get());
        when(deliveryClock.getZone()).thenReturn(KST);
        when(deliveryClock.withZone(any(ZoneId.class)))
            .thenAnswer(invocation -> Clock.fixed(now.get(), invocation.getArgument(0)));
        clear();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            // clear()가 지운 기존 singleton만 복원하며 새 cooldown은 NULL로 시작한다.
            jdbcTemplate.update("INSERT INTO dining_soldout_report_sequence (id, last_sequence) VALUES (1, 0)");
            coopShopFixture.현재학기();
            var student = userFixture.준호_학생(departmentFixture.컴퓨터공학부(), null).getUser();
            studentToken = userFixture.getToken(student);
            LocalDate today = now.get().atZone(KST).toLocalDate();
            diningId = diningFixture.A코너_점심(today).getId();
            otherDiningId = diningFixture.B코너_점심(today).getId();
        });
        when(s3Client.getDomainUrlPrefix()).thenReturn(IMAGE_DOMAIN);
        when(s3Client.isCustomDomainUrl(anyString()))
            .thenAnswer(invocation -> invocation.<String>getArgument(0).startsWith(IMAGE_DOMAIN));
        when(s3Client.extractKeyFromUrl(anyString()))
            .thenAnswer(invocation -> invocation.<String>getArgument(0).substring(IMAGE_DOMAIN.length()));
        when(s3Client.doesFileExist(anyString())).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        clear();
    }

    @Test
    void 본문과_멱등키_없는_claim은_서비스_인증과_빈_응답을_구분한다() throws Exception {
        mockMvc.perform(post(BOT_PATH + "/deliveries/claim")).andExpect(status().isUnauthorized());
        mockMvc.perform(post(BOT_PATH + "/deliveries/claim").header(BOT_HEADER, "wrong-token"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post(BOT_PATH + "/deliveries/claim").header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isUnauthorized());
        emptyClaim();
        int reportId = submit(diningId);
        JsonNode task = nextClaim();
        assertThat(task.path("report").path("report_id").asInt()).isEqualTo(reportId);
        assertThat(task.path("mode").asText()).isEqualTo("SEND");
        assertThat(task.path("operation").asText()).isEqualTo("CREATE");
        assertThat(task.path("target").path("workspace_id").asText()).isEqualTo("T_EXAMPLE");
        assertThat(task.path("target").path("channel_id").asText()).isEqualTo("C_EXAMPLE");
        assertThat(task.path("target").path("message_ts").isNull()).isTrue();
        assertThat(OffsetDateTime.parse(task.path("expires_at").asText()))
            .isEqualTo(START.atZone(KST).toOffsetDateTime().plusSeconds(60));
        emptyClaim();
        mockMvc.perform(post(BOT_PATH + "/deliveries/{id}/result", task.path("delivery_id").asText())
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of(
                    "attempt_token", task.path("attempt_token").asText(), "outcome", "UNCERTAIN"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void CREATE_성공_재통보는_문자열_ts를_보존하고_제보를_승인하지_않는다() throws Exception {
        int reportId = submit(diningId);
        JsonNode task = nextClaim();
        JsonNode completed = body(result(task, success(MESSAGE_TS)).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("DELIVERED")));
        assertThat(body(result(task, success(MESSAGE_TS)).andExpect(status().isOk()))).isEqualTo(completed);
        assertThat(reportStatus(reportId)).isEqualTo("PENDING");
        assertThat(jdbcTemplate.queryForObject("SELECT sold_out FROM dining_menus WHERE id = ?",
            Object.class, diningId)).isNull();
        emptyClaim();
        approve(reportId);
        JsonNode update = nextClaim();
        assertThat(update.path("operation").asText()).isEqualTo("UPDATE");
        assertThat(update.path("target").path("message_ts").asText()).isEqualTo(MESSAGE_TS);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT last_sequence FROM dining_soldout_report_sequence WHERE id = 1", Long.class)).isEqualTo(2L);
    }

    @Test
    void 최초_claim_전에_승인한_제보는_최신_상태로_CREATE한다() throws Exception {
        int reportId = submit(diningId);
        JsonNode approved = approve(reportId).path("report");
        JsonNode task = nextClaim();
        assertThat(task.path("operation").asText()).isEqualTo("CREATE");
        assertThat(task.path("report")).isEqualTo(approved);
        result(task, success(MESSAGE_TS)).andExpect(status().isOk());
        assertThat(reportStatus(reportId)).isEqualTo("APPROVED");
        emptyClaim();
    }

    @Test
    void 배정_뒤_승인해도_VERIFY는_원본을_확인하고_완료_후_새_UPDATE를_배정한다() throws Exception {
        int reportId = submit(diningId);
        JsonNode send = nextClaim();
        JsonNode approved = approve(reportId).path("report");
        emptyClaim();
        result(send, Map.of("outcome", "UNCERTAIN")).andExpect(status().isOk());
        JsonNode verify = nextClaim();
        assertSameWork(send, verify, "VERIFY");
        assertThat(verify.path("report").path("status").asText()).isEqualTo("PENDING");
        result(verify, success(MESSAGE_TS)).andExpect(status().isOk());
        JsonNode update = nextClaim();
        assertThat(update.path("delivery_id")).isNotEqualTo(send.path("delivery_id"));
        assertThat(update.path("operation").asText()).isEqualTo("UPDATE");
        assertThat(update.path("report")).isEqualTo(approved);
        assertThat(update.path("target").path("message_ts").asText()).isEqualTo(MESSAGE_TS);
        result(update, Map.of("outcome", "NOT_APPLIED", "reason", "REJECTED", "error_code", "message_not_found"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.delivery_state").value("NEEDS_ATTENTION"));
        advance(600);
        emptyClaim();
        assertThat(reportStatus(reportId)).isEqualTo("APPROVED");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void SEND_60초_만료와_VERIFY_실패는_재전송_없이_5분_뒤_다시_확인한다(boolean verifyExpires) throws Exception {
        submit(diningId);
        JsonNode send = nextClaim();
        advance(59);
        emptyClaim();
        advance(1);
        JsonNode verify = nextClaim();
        assertSameWork(send, verify, "VERIFY");
        if (verifyExpires) {
            advance(59);
            emptyClaim();
            advance(1);
            emptyClaim();
        } else {
            result(verify, Map.of("outcome", "UNCERTAIN")).andExpect(status().isOk())
                .andExpect(jsonPath("$.delivery_state").value("NEEDS_ATTENTION"));
        }
        advance(299);
        emptyClaim();
        advance(1);
        assertSameWork(send, nextClaim(), "VERIFY");
    }

    @Test
    void 늦은_SEND_성공은_VERIFY를_무효화하고_이후_불확실한_결과와_만료에도_유지한다() throws Exception {
        submit(diningId);
        JsonNode send = nextClaim();
        advance(60);
        JsonNode verify = nextClaim();
        result(send, success(MESSAGE_TS)).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("DELIVERED"));
        result(verify, Map.of("outcome", "UNCERTAIN")).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("DELIVERED"));
        advance(600);
        emptyClaim();
        result(send, success(MESSAGE_TS)).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("DELIVERED"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"NOT_SENT", "RATE_LIMITED", "REJECTED"})
    void VERIFY_만료_후_늦은_SEND_미반영_증거는_원인에_맞게_복구한다(String reason) throws Exception {
        submit(diningId);
        JsonNode send = nextClaim();
        advance(60);
        JsonNode verify = nextClaim();
        advance(60);
        emptyClaim();
        Map<String, Object> evidence = notApplied(reason, 30);
        String expected = reason.equals("REJECTED") ? "NEEDS_ATTENTION" : "QUEUED";
        result(send, evidence).andExpect(status().isOk()).andExpect(jsonPath("$.delivery_state").value(expected));
        result(verify, Map.of("outcome", "UNCERTAIN")).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value(expected));
        result(send, evidence).andExpect(status().isOk());
        if (reason.equals("REJECTED")) {
            advance(600);
            emptyClaim();
        } else {
            if (reason.equals("RATE_LIMITED")) {
                advance(29);
                emptyClaim();
                advance(1);
            }
            assertSameWork(send, nextClaim(), "SEND");
        }
    }

    @Test
    void 요청제한은_다른_제보에도_적용되고_더_긴_대기와_재통보를_보존한다() throws Exception {
        submit(diningId);
        submit(otherDiningId);
        JsonNode first = nextClaim();
        JsonNode second = nextClaim();
        result(first, notApplied("RATE_LIMITED", 30)).andExpect(status().isOk());
        result(second, Map.of("outcome", "UNCERTAIN")).andExpect(status().isOk());
        JsonNode verify = nextClaim();
        assertSameWork(second, verify, "VERIFY");
        advance(10);
        result(second, notApplied("RATE_LIMITED", 60)).andExpect(status().isOk());
        result(verify, Map.of("outcome", "UNCERTAIN")).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("QUEUED"));
        result(first, notApplied("RATE_LIMITED", 30)).andExpect(status().isOk());
        assertThat(jdbcTemplate.queryForObject(
            "SELECT delivery_cooldown_until FROM dining_soldout_report_sequence WHERE id = 1",
            LocalDateTime.class)).isEqualTo(START.plusSeconds(70).atZone(KST).toLocalDateTime());
        advance(59);
        emptyClaim();
        advance(1);
        JsonNode retry = nextClaim();
        assertThat(retry.path("mode").asText()).isEqualTo("SEND");
        assertThat(retry.path("report").path("report_id"))
            .isIn(first.path("report").path("report_id"), second.path("report").path("report_id"));
    }

    @Test
    void 보류된_제보의_늦은_요청제한도_다른_전송을_막고_재통보로_연장하지_않는다() throws Exception {
        int heldReportId = submit(diningId);
        int otherReportId = submit(otherDiningId);
        JsonNode send = nextClaim();
        assertThat(send.path("report").path("report_id").asInt()).isEqualTo(heldReportId);
        result(send, Map.of("outcome", "SUCCEEDED", "message_ref",
            Map.of("channel_id", "C_OTHER", "message_ts", MESSAGE_TS)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DINING_REPORT_DELIVERY_CONFLICT"));
        Map<String, Object> heldBinding = binding(heldReportId);
        assertThat(heldBinding).containsEntry("workspace_id", "T_EXAMPLE")
            .containsEntry("channel_id", "C_EXAMPLE").containsEntry("message_ts", null)
            .containsEntry("hold_reason", "CONFLICT");
        String attemptOutcomeSql = """
            SELECT accepted_outcome FROM dining_soldout_report_delivery_attempt
            WHERE token = UNHEX(REPLACE(?, '-', ''))
            """;
        assertThat(jdbcTemplate.queryForObject(attemptOutcomeSql, String.class,
            send.path("attempt_token").asText())).isNull();

        Map<String, Object> rateLimited = notApplied("RATE_LIMITED", 30);
        LocalDateTime deadline = now.get().atZone(KST).toLocalDateTime().plusSeconds(30);
        String cooldownSql = "SELECT delivery_cooldown_until FROM dining_soldout_report_sequence WHERE id = 1";
        result(send, rateLimited).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("NEEDS_ATTENTION"));
        assertThat(jdbcTemplate.queryForObject(attemptOutcomeSql, String.class,
            send.path("attempt_token").asText())).isEqualTo("NOT_APPLIED");
        assertThat(jdbcTemplate.queryForObject(cooldownSql, LocalDateTime.class)).isEqualTo(deadline);
        emptyClaim();

        advance(10);
        result(send, rateLimited).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("NEEDS_ATTENTION"));
        assertThat(jdbcTemplate.queryForObject(cooldownSql, LocalDateTime.class)).isEqualTo(deadline);
        assertThat(binding(heldReportId)).isEqualTo(heldBinding);
        advance(19);
        emptyClaim();
        advance(1);
        JsonNode next = nextClaim();
        assertThat(next.path("report").path("report_id").asInt()).isEqualTo(otherReportId);
        assertThat(next.path("mode").asText()).isEqualTo("SEND");
        assertThat(binding(heldReportId)).isEqualTo(heldBinding);
    }

    @ParameterizedTest
    @ValueSource(strings = {"different_ts", "wrong_channel", "not_applied", "update_target"})
    void 충돌_409는_기존_메시지_연결을_보존하고_다음_트랜잭션에도_전송을_보류한다(String conflict) throws Exception {
        int reportId = submit(diningId);
        JsonNode send = nextClaim();
        result(send, success(MESSAGE_TS)).andExpect(status().isOk());
        JsonNode task = send;
        if (conflict.equals("update_target")) {
            approve(reportId);
            task = nextClaim();
        }
        Map<String, Object> evidence = switch (conflict) {
            case "not_applied" -> notApplied("NOT_SENT", 0);
            case "wrong_channel" -> Map.of("outcome", "SUCCEEDED", "message_ref",
                Map.of("channel_id", "C_OTHER", "message_ts", MESSAGE_TS));
            default -> success("1791257400.000101");
        };
        result(task, evidence).andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DINING_REPORT_DELIVERY_CONFLICT"));
        assertThat(binding(reportId)).containsEntry("workspace_id", "T_EXAMPLE")
            .containsEntry("channel_id", "C_EXAMPLE").containsEntry("message_ts", MESSAGE_TS)
            .containsEntry("hold_reason", "CONFLICT");
        result(send, success(MESSAGE_TS)).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("NEEDS_ATTENTION"));
        if (!conflict.equals("update_target")) {
            approve(reportId);
        }
        advance(600);
        emptyClaim();
        assertThat(reportStatus(reportId)).isEqualTo("APPROVED");
    }

    @Test
    void 결과는_엄격한_본문_변형과_시도_소유권을_검증한다() throws Exception {
        submit(diningId);
        JsonNode task = nextClaim();
        Map<String, List<Map<String, Object>>> invalidBodies = Map.of(
            "INVALID_REQUEST_BODY", List.of(
                Map.of("outcome", "SUCCEEDED"),
                Map.of("outcome", "NOT_APPLIED"),
                Map.of("outcome", "NOT_APPLIED", "reason", "RATE_LIMITED"),
                notApplied("RATE_LIMITED", 0),
                Map.of("outcome", "NOT_APPLIED", "reason", "REJECTED", "error_code", ""),
                Map.of("outcome", "NOT_APPLIED", "reason", "REJECTED", "error_code", "x".repeat(129)),
                Map.of("outcome", "UNCERTAIN", "message_ref", Map.of("channel_id", "C_EXAMPLE", "message_ts", MESSAGE_TS)),
                Map.of("outcome", "UNCERTAIN", "reason", "NOT_SENT"),
                Map.of("outcome", "UNCERTAIN", "unexpected", true)),
            "NOT_READABLE_HTTP_MESSAGE", List.of(
                Map.of("outcome", "SUCCEEDED", "message_ref", Map.of("channel_id", "C_EXAMPLE", "message_ts", 123.5)),
                Map.of("outcome", "NOT_APPLIED", "reason", "RATE_LIMITED", "retry_after_seconds", 1.5)));
        for (var invalid : invalidBodies.entrySet()) {
            for (var request : invalid.getValue()) {
                result(task, request).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(invalid.getKey()));
            }
        }
        String deliveryId = task.path("delivery_id").asText();
        postResult(deliveryId, "{\"outcome\":\"UNCERTAIN\"}").andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
        postResult(deliveryId, """
            {"attempt_token":"%s","outcome":"UNCERTAIN","reason":null}
            """.formatted(task.path("attempt_token").asText()))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
        String validBody = objectMapper.writeValueAsString(Map.of(
            "attempt_token", task.path("attempt_token").asText(), "outcome", "UNCERTAIN"));
        JsonNode error = body(postResult("not-a-uuid", validBody).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"))
            .andExpect(jsonPath("$.status").doesNotHaveJsonPath()));
        assertThat(error.path("message").asText()).isNotBlank();
        assertThat(UUID.fromString(error.path("errorTraceId").asText()).toString())
            .isEqualTo(error.path("errorTraceId").asText());
        postResult("1-1-1-1-1", validBody).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));
        postResult(deliveryId, "{\"attempt_token\":\"not-a-uuid\",\"outcome\":\"UNCERTAIN\"}")
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("NOT_READABLE_HTTP_MESSAGE"));
        String unknownAttempt = objectMapper.writeValueAsString(Map.of(
            "attempt_token", UUID.randomUUID().toString(), "outcome", "UNCERTAIN"));
        postResult(deliveryId, unknownAttempt).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND_DINING_REPORT_DELIVERY"));
        postResult(UUID.randomUUID().toString(), unknownAttempt).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND_DINING_REPORT_DELIVERY"));
        result(task, Map.of("outcome", "UNCERTAIN")).andExpect(status().isOk());
        JsonNode verify = nextClaim();
        result(verify, notApplied("NOT_SENT", 0)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
        result(verify, success(MESSAGE_TS)).andExpect(status().isOk());
    }

    @Test
    void 동시_claim은_한_제보에_SEND를_한번만_배정한다() throws Exception {
        int reportId = submit(diningId);
        List<MvcResult> responses = concurrentlyAtGate(() -> claim().andReturn(), () -> claim().andReturn());
        assertThat(responses).extracting(response -> response.getResponse().getStatus())
            .containsExactlyInAnyOrder(200, 204);
        JsonNode task = body(responses.stream().filter(response -> response.getResponse().getStatus() == 200)
            .findFirst().orElseThrow());
        assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM dining_soldout_report_delivery WHERE report_id = ?", Integer.class, reportId)).isOne();
        assertThat(jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM dining_soldout_report_delivery_attempt a
            JOIN dining_soldout_report_delivery d ON d.id = a.delivery_id WHERE d.report_id = ?
            """, Integer.class, reportId)).isOne();
        emptyClaim();
        result(task, success(MESSAGE_TS)).andExpect(status().isOk());
        emptyClaim();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 동시_성공_증거는_같으면_멱등이고_다르면_연결을_보존하며_보류한다(boolean conflicting) throws Exception {
        int reportId = submit(diningId);
        JsonNode task = nextClaim();
        List<MvcResult> responses = concurrentlyAtGate(
            () -> result(task, success(MESSAGE_TS)).andReturn(),
            () -> result(task, success(conflicting ? "1791257400.000101" : MESSAGE_TS)).andReturn());
        assertThat(responses).extracting(response -> response.getResponse().getStatus())
            .containsExactlyInAnyOrder(200, conflicting ? 409 : 200);
        String winningTs = responses.get(0).getResponse().getStatus() == 200 ? MESSAGE_TS : "1791257400.000101";
        assertThat(binding(reportId)).containsEntry("message_ts", winningTs);
        assertThat(reportStatus(reportId)).isEqualTo("PENDING");
        approve(reportId);
        if (conflicting) {
            assertThat(binding(reportId)).containsEntry("hold_reason", "CONFLICT");
            emptyClaim();
        } else {
            JsonNode update = nextClaim();
            assertThat(update.path("operation").asText()).isEqualTo("UPDATE");
            assertThat(update.path("target").path("message_ts").asText()).isEqualTo(MESSAGE_TS);
        }
    }

    private int submit(Integer targetDiningId) throws Exception {
        return body(mockMvc.perform(post("/dinings/{id}/soldout-reports", targetDiningId)
                .header("Authorization", "Bearer " + studentToken)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("image_url", IMAGE_URL))))
            .andExpect(status().isCreated())).path("report_id").asInt();
    }

    private JsonNode approve(int reportId) throws Exception {
        return body(mockMvc.perform(post(BOT_PATH + "/{id}/approve", reportId).header(BOT_HEADER, BOT_TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of("actor",
                    Map.of("workspace_id", "T_EXAMPLE", "user_id", "U_REVIEWER", "display_name", "담당자")))))
            .andExpect(status().isOk()));
    }

    private ResultActions claim() throws Exception {
        return mockMvc.perform(post(BOT_PATH + "/deliveries/claim").header(BOT_HEADER, BOT_TOKEN));
    }

    private JsonNode nextClaim() throws Exception {
        return body(claim().andExpect(status().isOk()));
    }

    private void emptyClaim() throws Exception {
        claim().andExpect(status().isNoContent()).andExpect(header().string("Retry-After", "5"))
            .andExpect(content().string(""));
    }

    private ResultActions result(JsonNode task, Map<String, Object> evidence) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>(evidence);
        request.put("attempt_token", task.path("attempt_token").asText());
        return postResult(task.path("delivery_id").asText(), objectMapper.writeValueAsString(request));
    }

    private ResultActions postResult(String deliveryId, String request) throws Exception {
        return mockMvc.perform(post(BOT_PATH + "/deliveries/{id}/result", deliveryId).header(BOT_HEADER, BOT_TOKEN)
            .contentType(MediaType.APPLICATION_JSON).content(request));
    }

    private Map<String, Object> success(String messageTs) {
        return Map.of("outcome", "SUCCEEDED", "message_ref", Map.of("channel_id", "C_EXAMPLE", "message_ts", messageTs));
    }

    private Map<String, Object> notApplied(String reason, long retryAfter) {
        return switch (reason) {
            case "RATE_LIMITED" -> Map.of("outcome", "NOT_APPLIED", "reason", reason, "retry_after_seconds", retryAfter);
            case "REJECTED" -> Map.of("outcome", "NOT_APPLIED", "reason", reason, "error_code", "channel_not_found");
            default -> Map.of("outcome", "NOT_APPLIED", "reason", reason);
        };
    }

    private void assertSameWork(JsonNode original, JsonNode next, String mode) {
        assertThat(next.path("delivery_id")).isEqualTo(original.path("delivery_id"));
        assertThat(next.path("attempt_token")).isNotEqualTo(original.path("attempt_token"));
        assertThat(next.path("mode").asText()).isEqualTo(mode);
        assertThat(next.path("operation")).isEqualTo(original.path("operation"));
        assertThat(next.path("target")).isEqualTo(original.path("target"));
        assertThat(next.path("report")).isEqualTo(original.path("report"));
    }

    private void advance(long seconds) {
        now.updateAndGet(instant -> instant.plusSeconds(seconds));
    }

    private String reportStatus(int reportId) {
        return jdbcTemplate.queryForObject("SELECT status FROM dining_soldout_report WHERE id = ?", String.class, reportId);
    }

    private Map<String, Object> binding(int reportId) {
        return jdbcTemplate.queryForMap("""
            SELECT workspace_id, channel_id, message_ts, hold_reason
            FROM dining_soldout_report_delivery_target WHERE report_id = ?
            """, reportId);
    }

    private JsonNode body(ResultActions response) throws Exception {
        return body(response.andReturn());
    }

    private JsonNode body(MvcResult response) throws Exception {
        return objectMapper.readTree(response.getResponse().getContentAsByteArray());
    }

    private List<MvcResult> concurrentlyAtGate(Callable<MvcResult> first, Callable<MvcResult> second) throws Exception {
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
                        throw new IllegalStateException("첫 요청의 순번 잠금 해제 대기 시간 초과");
                    }
                    return sequence;
                }
                secondAtGate.countDown();
                var sequence = originalAnswer.answer(invocation);
                secondLocked.countDown();
                return sequence;
            }).when(sequenceRepository).findForUpdate();

            var firstResponse = executor.submit(first);
            assertThat(firstLocked.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            var secondResponse = executor.submit(second);
            assertThat(secondAtGate.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            // 첫 요청이 실제 DB 잠금을 소유하는 동안 두 번째 잠금 호출은 반환할 수 없다.
            assertThat(secondLocked.await(200, TimeUnit.MILLISECONDS)).isFalse();
            assertThat(secondResponse.isDone()).isFalse();
            releaseFirst.countDown();
            List<MvcResult> responses = List.of(firstResponse.get(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                secondResponse.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertThat(secondLocked.getCount()).isZero();
            assertThat(acquisitions.get()).isEqualTo(2);
            return responses;
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
