package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
@TestPropertySource(properties = "dining.report.bot-token=test-dining-report-bot-token")
class DiningReportDeliveryApiTest extends AcceptanceTest {

    private static final String BOT_PATH = "/internal/dining/soldout-reports";
    private static final String BOT_HEADER = "X-Koin-Service-Token";
    private static final String BOT_TOKEN = "test-dining-report-bot-token";
    private static final String IMAGE_DOMAIN = "https://test.koreatech.in/";
    private static final String IMAGE_URL = IMAGE_DOMAIN
        + "upload/COOP/2024/1/15/e924c7d3-3757-4cd0-961b-e65c1f6cc8ad/soldout.jpg";
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
    void 봇_토큰만으로_작업을_배정하고_두_필드_성공_결과로_완료한다() throws Exception {
        int reportId = submit(diningId);
        JsonNode task = nextClaim();
        assertThat(task.fieldNames()).toIterable()
            .containsExactlyInAnyOrder("delivery_id", "attempt_token", "expires_at", "report");
        assertThat(task.path("report").path("report_id").asInt()).isEqualTo(reportId);
        assertThat(OffsetDateTime.parse(task.path("expires_at").asText()))
            .isEqualTo(START.atZone(KST).toOffsetDateTime().plusSeconds(60));
        postResult(task.path("delivery_id").asText(), objectMapper.writeValueAsString(Map.of(
                "attempt_token", task.path("attempt_token").asText(), "outcome", "SUCCEEDED")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_id").value(task.path("delivery_id").asText()))
            .andExpect(jsonPath("$.delivery_state").value("DELIVERED"));
        emptyClaim();
    }

    @Test
    void 서비스_토큰으로만_배정과_결과를_인증하고_빈_작업은_204를_반환한다() throws Exception {
        mockMvc.perform(post(BOT_PATH + "/deliveries/claim")).andExpect(status().isUnauthorized());
        mockMvc.perform(post(BOT_PATH + "/deliveries/claim").header(BOT_HEADER, "wrong-token"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post(BOT_PATH + "/deliveries/claim").header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isUnauthorized());
        emptyClaim();
        submit(diningId);
        JsonNode task = nextClaim();
        emptyClaim();
        mockMvc.perform(post(BOT_PATH + "/deliveries/{id}/result", task.path("delivery_id").asText())
                .contentType(MediaType.APPLICATION_JSON).content(resultBody(task, "SUCCEEDED")))
            .andExpect(status().isUnauthorized());
        result(task, "SUCCEEDED").andExpect(status().isOk());
    }

    @Test
    void 성공_재통보는_멱등이며_제보_업무와_후속_변경을_완료하지_않는다() throws Exception {
        int reportId = submit(diningId);
        JsonNode task = nextClaim();
        JsonNode completed = body(result(task, "SUCCEEDED").andExpect(status().isOk()));
        assertThat(completed.fieldNames()).toIterable().containsExactlyInAnyOrder("delivery_id", "delivery_state");
        assertThat(completed.path("delivery_state").asText()).isEqualTo("DELIVERED");
        assertThat(body(result(task, "SUCCEEDED").andExpect(status().isOk()))).isEqualTo(completed);
        assertThat(reportStatus(reportId)).isEqualTo("PENDING");
        assertThat(jdbcTemplate.queryForObject("SELECT sold_out FROM dining_menus WHERE id = ?",
            Object.class, diningId)).isNull();
        result(task, "FAILED").andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DINING_REPORT_DELIVERY_CONFLICT"));
        emptyClaim();
        JsonNode approved = approve(reportId).path("report");
        JsonNode next = nextClaim();
        assertThat(next.path("delivery_id")).isNotEqualTo(task.path("delivery_id"));
        assertThat(next.path("report")).isEqualTo(approved);
        result(task, "SUCCEEDED").andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("DELIVERED"));
        emptyClaim();
        result(next, "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
    }

    @Test
    void 최초_배정_전에_승인해도_생성과_처리_스냅샷을_순서대로_배정한다() throws Exception {
        int reportId = submit(diningId);
        JsonNode approved = approve(reportId).path("report");
        JsonNode created = nextClaim();
        assertThat(created.path("report").path("status").asText()).isEqualTo("PENDING");
        assertThat(created.path("report").path("processor").isNull()).isTrue();
        emptyClaim();
        result(created, "SUCCEEDED").andExpect(status().isOk());
        JsonNode processed = nextClaim();
        assertThat(processed.path("delivery_id")).isNotEqualTo(created.path("delivery_id"));
        assertThat(processed.path("report")).isEqualTo(approved);
        result(processed, "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
    }

    @Test
    void 실패는_5초_후_같은_작업을_재배정하고_재통보가_대기를_연장하지_않는다() throws Exception {
        int reportId = submit(diningId);
        JsonNode task = nextClaim();
        JsonNode approved = approve(reportId).path("report");
        result(task, "FAILED").andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("QUEUED"));
        advance(4);
        result(task, "FAILED").andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("QUEUED"));
        result(task, "SUCCEEDED").andExpect(status().isConflict());
        emptyClaim();
        advance(1);
        JsonNode retry = nextClaim();
        assertSameWork(task, retry);
        result(task, "FAILED").andExpect(status().isConflict());
        result(task, "SUCCEEDED").andExpect(status().isConflict());
        result(retry, "SUCCEEDED").andExpect(status().isOk());
        JsonNode processed = nextClaim();
        assertThat(processed.path("report")).isEqualTo(approved);
        result(processed, "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
    }

    @Test
    void 한_제보의_실패_대기는_다른_제보의_배정을_막지_않는다() throws Exception {
        int firstReportId = submit(diningId);
        int secondReportId = submit(otherDiningId);
        JsonNode first = nextClaim();
        assertThat(first.path("report").path("report_id").asInt()).isEqualTo(firstReportId);
        result(first, "FAILED").andExpect(status().isOk());
        approve(firstReportId);
        JsonNode second = nextClaim();
        assertThat(second.path("report").path("report_id").asInt()).isEqualTo(secondReportId);
        result(second, "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
        advance(5);
        assertSameWork(first, nextClaim());
    }

    @Test
    void 구버전의_늦은_변경_INSERT는_첫_claim에서_UUID와_스냅샷을_초기화한다() throws Exception {
        int existingReportId = submit(diningId);
        result(nextClaim(), "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
        int legacyReportId = new TransactionTemplate(transactionManager).execute(transaction -> {
            jdbcTemplate.update("""
                INSERT INTO dining_soldout_report
                    (dining_id, reporter_id, image_url, status, request_key, created_at, updated_at)
                SELECT ?, reporter_id, image_url, 'PENDING', UNHEX(REPLACE(?, '-', '')), created_at, updated_at
                FROM dining_soldout_report WHERE id = ?
                """, otherDiningId, UUID.randomUUID().toString(), existingReportId);
            int reportId = jdbcTemplate.queryForObject(
                "SELECT id FROM dining_soldout_report WHERE dining_id = ?", Integer.class, otherDiningId);
            long sequence = sequenceRepository.findForUpdate().next();
            jdbcTemplate.update("""
                INSERT INTO dining_soldout_report_change
                    (sequence, report_id, event_type, status, processing_type, processing_id, occurred_at)
                VALUES (?, ?, 'CREATED', 'PENDING', NULL, NULL, ?)
                """, sequence, reportId, now.get().atZone(KST).toLocalDateTime());
            return reportId;
        });
        assertThat(jdbcTemplate.queryForObject(
            "SELECT last_sequence FROM dining_soldout_report_sequence WHERE id = 1", Long.class)).isEqualTo(2L);
        assertThat(jdbcTemplate.queryForMap("""
            SELECT delivery_id, report_snapshot, delivery_state, attempt_token, expires_at, next_attempt_at, accepted_outcome
            FROM dining_soldout_report_change WHERE report_id = ?
            """, legacyReportId))
            .containsEntry("delivery_id", null).containsEntry("report_snapshot", null)
            .containsEntry("delivery_state", "QUEUED").containsEntry("attempt_token", null)
            .containsEntry("expires_at", null).containsEntry("next_attempt_at", null).containsEntry("accepted_outcome", null);
        JsonNode expectedReport = body(mockMvc.perform(get(BOT_PATH + "/{id}", legacyReportId)
            .header(BOT_HEADER, BOT_TOKEN)).andExpect(status().isOk()));
        JsonNode task = nextClaim();
        String deliveryId = task.path("delivery_id").asText();
        assertThat(UUID.fromString(deliveryId).toString()).isEqualTo(deliveryId);
        assertThat(task.path("report")).isEqualTo(expectedReport);
        String storedSnapshot = jdbcTemplate.queryForObject("""
            SELECT report_snapshot FROM dining_soldout_report_change
            WHERE report_id = ? AND delivery_id = UNHEX(REPLACE(?, '-', ''))
            """, String.class, legacyReportId, deliveryId);
        assertThat(objectMapper.readTree(storedSnapshot)).isEqualTo(expectedReport);
        result(task, "SUCCEEDED").andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("DELIVERED"));
        emptyClaim();
        JsonNode approved = approve(legacyReportId).path("report");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT last_sequence FROM dining_soldout_report_sequence WHERE id = 1", Long.class)).isEqualTo(3L);
        JsonNode processed = nextClaim();
        assertThat(processed.path("report")).isEqualTo(approved);
        result(processed, "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
    }

    @Test
    void 배정_60초_만료_즉시_새_토큰을_배정하고_이전_토큰의_결과는_거부한다() throws Exception {
        int reportId = submit(diningId);
        JsonNode first = nextClaim();
        JsonNode approved = approve(reportId).path("report");
        advance(59);
        emptyClaim();
        advance(1);
        JsonNode replacement = nextClaim();
        assertSameWork(first, replacement);
        assertThat(OffsetDateTime.parse(replacement.path("expires_at").asText()))
            .isEqualTo(START.atZone(KST).toOffsetDateTime().plusSeconds(120));
        for (String outcome : List.of("SUCCEEDED", "FAILED")) {
            result(first, outcome).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DINING_REPORT_DELIVERY_CONFLICT"));
        }
        emptyClaim();
        result(replacement, "SUCCEEDED").andExpect(status().isOk());
        JsonNode processed = nextClaim();
        assertThat(processed.path("report")).isEqualTo(approved);
        result(processed, "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUCCEEDED", "FAILED"})
    void 만료되어도_재배정되지_않은_현재_토큰의_늦은_결과를_접수한다(String outcome) throws Exception {
        submit(diningId);
        JsonNode task = nextClaim();
        advance(61);
        String expectedState = outcome.equals("SUCCEEDED") ? "DELIVERED" : "QUEUED";
        result(task, outcome).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value(expectedState));
        result(task, outcome).andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value(expectedState));
        result(task, outcome.equals("SUCCEEDED") ? "FAILED" : "SUCCEEDED").andExpect(status().isConflict());
        emptyClaim();
        if (outcome.equals("FAILED")) {
            advance(5);
            JsonNode retry = nextClaim();
            assertSameWork(task, retry);
            result(retry, "SUCCEEDED").andExpect(status().isOk());
        }
        emptyClaim();
    }

    @Test
    void 없는_작업과_다른_작업의_토큰은_404이고_알_수_없는_현재_토큰은_409이다() throws Exception {
        submit(diningId);
        submit(otherDiningId);
        JsonNode first = nextClaim();
        JsonNode second = nextClaim();
        postResult(UUID.randomUUID().toString(), resultBody(first, "SUCCEEDED"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND_DINING_REPORT_DELIVERY"));
        postResult(first.path("delivery_id").asText(), resultBody(second, "SUCCEEDED"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND_DINING_REPORT_DELIVERY"));
        postResult(first.path("delivery_id").asText(), objectMapper.writeValueAsString(Map.of(
                "attempt_token", UUID.randomUUID().toString(), "outcome", "SUCCEEDED")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DINING_REPORT_DELIVERY_CONFLICT"));
        result(first, "SUCCEEDED").andExpect(status().isOk());
        result(second, "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
    }

    @ParameterizedTest
    @ValueSource(strings = {"message_ref", "reason", "error_code", "retry_after_seconds", "mode", "operation", "target", "unexpected"})
    void 결과의_미지원_필드는_null이어도_거부한다(String field) throws Exception {
        submit(diningId);
        JsonNode task = nextClaim();
        for (String outcome : List.of("SUCCEEDED", "FAILED")) {
            for (Object value : new Object[] {null, true}) {
                Map<String, Object> request = new LinkedHashMap<>();
                request.put("attempt_token", task.path("attempt_token").asText());
                request.put("outcome", outcome);
                request.put(field, value);
                postResult(task.path("delivery_id").asText(), objectMapper.writeValueAsString(request))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
            }
        }
        result(task, "SUCCEEDED").andExpect(status().isOk());
    }

    @Test
    void 결과는_필수_문자열과_두_결과값과_정규_UUID를_검증한다() throws Exception {
        submit(diningId);
        JsonNode task = nextClaim();
        String deliveryId = task.path("delivery_id").asText();
        String token = task.path("attempt_token").asText();
        Map<String, List<String>> invalidBodies = Map.of(
            "INVALID_REQUEST_BODY", List.of(
                "{}", "{\"outcome\":\"SUCCEEDED\"}",
                "{\"attempt_token\":null,\"outcome\":\"SUCCEEDED\"}",
                "{\"attempt_token\":\"\",\"outcome\":\"SUCCEEDED\"}",
                "{\"attempt_token\":\"%s\"}".formatted(token),
                "{\"attempt_token\":\"%s\",\"outcome\":null}".formatted(token),
                "{\"attempt_token\":\"%s\",\"outcome\":\"\"}".formatted(token)),
            "NOT_READABLE_HTTP_MESSAGE", List.of(
                "null", "[]", "true", "{\"attempt_token\":123,\"outcome\":\"SUCCEEDED\"}",
                "{\"attempt_token\":\"not-a-uuid\",\"outcome\":\"SUCCEEDED\"}",
                "{\"attempt_token\":\"1-1-1-1-1\",\"outcome\":\"SUCCEEDED\"}",
                "{\"attempt_token\":\"%s\",\"outcome\":true}".formatted(token),
                "{\"attempt_token\":\"%s\",\"outcome\":\"UNCERTAIN\"}".formatted(token),
                "{\"attempt_token\":\"%s\",\"outcome\":\"NOT_APPLIED\"}".formatted(token),
                "{\"attempt_token\":\"%s\",\"outcome\":\"succeeded\"}".formatted(token)));
        for (var invalid : invalidBodies.entrySet()) {
            for (String request : invalid.getValue()) {
                postResult(deliveryId, request).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(invalid.getKey()));
            }
        }
        for (String invalidId : List.of("not-a-uuid", "1-1-1-1-1")) {
            JsonNode error = body(postResult(invalidId, resultBody(task, "SUCCEEDED"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"))
                .andExpect(jsonPath("$.status").doesNotHaveJsonPath()));
            assertThat(error.path("message").asText()).isNotBlank();
            assertThat(UUID.fromString(error.path("errorTraceId").asText()).toString())
                .isEqualTo(error.path("errorTraceId").asText());
        }
        result(task, "SUCCEEDED").andExpect(status().isOk());
    }

    @Test
    void 동시_claim은_하나의_작업에_하나의_시도만_배정한다() throws Exception {
        int reportId = submit(diningId);
        List<MvcResult> responses = concurrentlyAtGate(() -> claim().andReturn(), () -> claim().andReturn());
        assertThat(responses).extracting(response -> response.getResponse().getStatus())
            .containsExactlyInAnyOrder(200, 204);
        JsonNode task = body(responses.stream().filter(response -> response.getResponse().getStatus() == 200)
            .findFirst().orElseThrow());
        assertThat(jdbcTemplate.queryForObject("""
            SELECT COUNT(*) FROM dining_soldout_report_change
            WHERE report_id = ? AND delivery_state = 'IN_PROGRESS'
                AND delivery_id = UNHEX(REPLACE(?, '-', '')) AND attempt_token = UNHEX(REPLACE(?, '-', ''))
            """, Integer.class, reportId, task.path("delivery_id").asText(), task.path("attempt_token").asText())).isOne();
        emptyClaim();
        result(task, "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 동시_결과는_같으면_멱등이고_다르면_409이며_후속_작업을_보류하지_않는다(boolean conflicting) throws Exception {
        int reportId = submit(diningId);
        JsonNode task = nextClaim();
        List<MvcResult> responses = concurrentlyAtGate(
            () -> result(task, "SUCCEEDED").andReturn(),
            () -> result(task, conflicting ? "FAILED" : "SUCCEEDED").andReturn());
        assertThat(responses).extracting(response -> response.getResponse().getStatus())
            .containsExactlyInAnyOrder(200, conflicting ? 409 : 200);
        result(task, "SUCCEEDED").andExpect(status().isOk())
            .andExpect(jsonPath("$.delivery_state").value("DELIVERED"));
        assertThat(reportStatus(reportId)).isEqualTo("PENDING");
        JsonNode approved = approve(reportId).path("report");
        JsonNode processed = nextClaim();
        assertThat(processed.path("report")).isEqualTo(approved);
        result(processed, "SUCCEEDED").andExpect(status().isOk());
        emptyClaim();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/v3/api-docs", "/v3/api-docs/3. Campus API"})
    void 기본과_Campus_스키마는_단순한_닫힌_작업_객체와_해결된_참조를_제공한다(String path) throws Exception {
        JsonNode document = body(mockMvc.perform(get(path)).andExpect(status().isOk()));
        JsonNode schemas = document.path("components").path("schemas");
        assertClosedSchema(schemas.path("DiningReportDeliveryResponse"),
            "delivery_id", "attempt_token", "expires_at", "report");
        assertClosedSchema(schemas.path("DiningReportDeliveryResultRequest"), "attempt_token", "outcome");
        assertClosedSchema(schemas.path("DiningReportDeliveryResultResponse"), "delivery_id", "delivery_state");
        assertThat(schemas.path("DiningReportDeliveryResultRequest").path("properties").path("outcome").path("enum"))
            .isEqualTo(objectMapper.valueToTree(List.of("SUCCEEDED", "FAILED")));
        assertThat(schemas.path("DiningReportDeliveryResultResponse").path("properties").path("delivery_state").path("enum"))
            .isEqualTo(objectMapper.valueToTree(List.of("QUEUED", "IN_PROGRESS", "DELIVERED")));
        assertThat(schemas.fieldNames()).toIterable().doesNotContain("DiningReportDeliveryMessageTarget",
            "DiningReportDeliveryMessageReference", "DiningReportDeliverySucceeded", "DiningReportDeliveryNotSent",
            "DiningReportDeliveryRateLimited", "DiningReportDeliveryRejected", "DiningReportDeliveryUncertain");
        List<String> references = new ArrayList<>();
        collectReferences(document, references);
        assertThat(references.stream().filter(reference -> reference.startsWith("#/components/schemas/"))
            .filter(reference -> document.at(reference.substring(1)).isMissingNode()).distinct().toList()).isEmpty();
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

    private ResultActions result(JsonNode task, String outcome) throws Exception {
        return postResult(task.path("delivery_id").asText(), resultBody(task, outcome));
    }

    private String resultBody(JsonNode task, String outcome) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "attempt_token", task.path("attempt_token").asText(), "outcome", outcome));
    }

    private ResultActions postResult(String deliveryId, String request) throws Exception {
        return mockMvc.perform(post(BOT_PATH + "/deliveries/{id}/result", deliveryId).header(BOT_HEADER, BOT_TOKEN)
            .contentType(MediaType.APPLICATION_JSON).content(request));
    }

    private void assertSameWork(JsonNode original, JsonNode next) {
        assertThat(next.path("delivery_id")).isEqualTo(original.path("delivery_id"));
        assertThat(next.path("attempt_token")).isNotEqualTo(original.path("attempt_token"));
        assertThat(next.path("report")).isEqualTo(original.path("report"));
    }

    private void assertClosedSchema(JsonNode schema, String... fields) {
        assertThat(schema.path("type").asText()).isEqualTo("object");
        assertThat(schema.path("additionalProperties").asBoolean(true)).isFalse();
        assertThat(schema.path("properties").fieldNames()).toIterable().containsExactlyInAnyOrder(fields);
        assertThat(objectMapper.convertValue(schema.path("required"), String[].class)).containsExactlyInAnyOrder(fields);
        assertThat(schema.has("oneOf")).isFalse();
        assertThat(schema.has("anyOf")).isFalse();
    }

    private void collectReferences(JsonNode node, List<String> references) {
        if (node.isObject() && node.has("$ref")) {
            references.add(node.path("$ref").asText());
        }
        node.elements().forEachRemaining(child -> collectReferences(child, references));
    }

    private void advance(long seconds) {
        now.updateAndGet(instant -> instant.plusSeconds(seconds));
    }

    private String reportStatus(int reportId) {
        return jdbcTemplate.queryForObject("SELECT status FROM dining_soldout_report WHERE id = ?", String.class, reportId);
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
