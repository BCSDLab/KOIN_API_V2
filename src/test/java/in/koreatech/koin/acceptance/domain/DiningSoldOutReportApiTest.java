package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CoopShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DiningAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.infrastructure.s3.client.S3Client;

@TestPropertySource(properties = "dining.report.bot-token=test-dining-report-bot-token")
class DiningSoldOutReportApiTest extends AcceptanceTest {

    private static final String BOT_PATH = "/internal/dining/soldout-reports";
    private static final String ADMIN_PATH = "/admin/dining/soldout-reports";
    private static final String BOT_HEADER = "X-Koin-Service-Token";
    private static final String BOT_TOKEN = "test-dining-report-bot-token";
    private static final String IMAGE_DOMAIN = "https://test.koreatech.in/";
    private static final String IMAGE_URL = IMAGE_DOMAIN
        + "upload/COOP/2024/1/15/e924c7d3-3757-4cd0-961b-e65c1f6cc8ad/soldout.jpg";

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
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private S3Client s3Client;

    private User student;
    private String studentToken;
    private String otherStudentToken;
    private String thirdStudentToken;
    private String generalToken;
    private String coopToken;
    private String adminToken;
    private Dining dining;
    private Dining otherDining;

    @BeforeEach
    void setUp() {
        clear();
        // clear()가 지운 운영 마이그레이션의 sequence seed만 복원한다.
        entityManager.createNativeQuery(
            "INSERT INTO dining_soldout_report_sequence (id, last_sequence) VALUES (1, 0)").executeUpdate();
        coopShopFixture.현재학기();
        var department = departmentFixture.컴퓨터공학부();
        student = userFixture.준호_학생(department, null).getUser();
        studentToken = userFixture.getToken(student);
        User otherStudent = userFixture.성빈_학생(department).getUser();
        otherStudentToken = userFixture.getToken(otherStudent);
        // 기존 두 학생 fixture의 익명 닉네임 UNIQUE 충돌을 피한다.
        entityManager.createNativeQuery("UPDATE users SET anonymous_nickname = '품절제보_성빈' WHERE id = :id")
            .setParameter("id", otherStudent.getId()).executeUpdate();
        thirdStudentToken = userFixture.getToken(userFixture.익명_학생(department).getUser());
        coopToken = userFixture.getToken(userFixture.준기_영양사().getUser());
        adminToken = userFixture.getToken(userFixture.코인_운영자().getUser());
        generalToken = userFixture.getToken(userRepository.save(User.builder()
            .loginPw(student.getLoginPw())
            .userType(UserType.GENERAL)
            .isAuthed(true)
            .isDeleted(false)
            .build()));
        LocalDate today = LocalDate.now(clock.withZone(ZoneId.of("Asia/Seoul")));
        dining = diningFixture.A코너_점심(today);
        otherDining = diningFixture.B코너_점심(today);
        when(s3Client.getDomainUrlPrefix()).thenReturn(IMAGE_DOMAIN);
        when(s3Client.isCustomDomainUrl(anyString()))
            .thenAnswer(invocation -> invocation.<String>getArgument(0).startsWith(IMAGE_DOMAIN));
        when(s3Client.extractKeyFromUrl(anyString()))
            .thenAnswer(invocation -> invocation.<String>getArgument(0).substring(IMAGE_DOMAIN.length()));
        when(s3Client.doesFileExist(anyString())).thenReturn(true);
    }

    @Test
    void 접수는_판매상태를_유지하고_재시도와_중복을_구분한다() throws Exception {
        String key = UUID.randomUUID().toString();
        int id = body(submit(studentToken, dining, key, IMAGE_URL).andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))).path("report_id").asInt();
        botDetail(id).andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.image_url").value(IMAGE_URL));
        assertThat(storedDining().getSoldOut()).isNull();
        submit(studentToken, dining, key, IMAGE_URL).andExpect(status().isCreated())
            .andExpect(jsonPath("$.report_id").value(id));
        submit(studentToken, dining, key, IMAGE_URL.replace("soldout.jpg", "other.jpg"))
            .andExpect(status().isConflict());
        submit(studentToken, otherDining, key, IMAGE_URL).andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_CONFLICT"));
        submit(studentToken, dining, UUID.randomUUID().toString(), IMAGE_URL).andExpect(status().isConflict());
    }

    @Test
    void 접수는_학생과_당일식단과_업로드사진을_검증한다() throws Exception {
        String key = UUID.randomUUID().toString();
        for (String token : List.of(generalToken, coopToken, adminToken)) {
            submit(token, dining, key, IMAGE_URL).andExpect(status().isForbidden());
        }
        submit(null, dining, key, IMAGE_URL).andExpect(status().isUnauthorized());
        for (int days : List.of(-1, 1)) {
            Dining anotherDate = diningFixture.A코너_점심(dining.getDate().plusDays(days));
            submit(studentToken, anotherDate, key, IMAGE_URL).andExpect(status().isBadRequest());
        }
        submit(studentToken, dining, key, "https://outside.example/soldout.jpg").andExpect(status().isBadRequest());
        when(s3Client.doesFileExist(anyString())).thenReturn(false);
        submit(studentToken, dining, key, IMAGE_URL).andExpect(status().isBadRequest());
        student.updateAuthenticationStatus(false);
        entityManager.flush();
        submit(studentToken, dining, key, IMAGE_URL).andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("FORBIDDEN_STUDENT"));
    }

    @Test
    void 반려는_대상만_승인은_남은_대기를_처리하고_재처리는_최초결과를_유지한다() throws Exception {
        int rejectedId = createReport(studentToken, dining);
        int selectedId = createReport(otherStudentToken, dining);
        int automaticId = createReport(thirdStudentToken, dining);
        int unrelatedId = createReport(studentToken, otherDining);
        JsonNode rejected = body(decide(rejectedId, "reject", "U_FIRST").andExpect(status().isOk()));
        JsonNode replayedRejection = body(decide(rejectedId, "reject", "U_OTHER").andExpect(status().isOk())
            .andExpect(jsonPath("$.already_processed").value(true))
            .andExpect(jsonPath("$.processing_id").value(rejected.path("processing_id").asText()))
            .andExpect(jsonPath("$.affected_report_ids").value(contains(rejectedId))));
        assertThat(replayedRejection.path("report")).isEqualTo(rejected.path("report"));
        botDetail(selectedId).andExpect(jsonPath("$.status").value("PENDING"));
        assertThat(storedDining().getSoldOut()).isNull();
        JsonNode approved = body(decide(selectedId, "approve", "U_FIRST").andExpect(status().isOk())
            .andExpect(jsonPath("$.report.status").value("APPROVED"))
            .andExpect(jsonPath("$.report.processing_type").value("MANUAL"))
            .andExpect(jsonPath("$.affected_report_ids").value(contains(selectedId, automaticId))));
        String group = approved.path("processing_id").asText();
        botDetail(automaticId).andExpect(jsonPath("$.status").value("APPROVED"))
            .andExpect(jsonPath("$.processing_type").value("SAME_DINING_APPROVED"))
            .andExpect(jsonPath("$.source_report_id").value(selectedId));
        decide(automaticId, "approve", "U_OTHER").andExpect(status().isOk())
            .andExpect(jsonPath("$.already_processed").value(true))
            .andExpect(jsonPath("$.processing_id").value(group))
            .andExpect(jsonPath("$.affected_report_ids").value(contains(selectedId, automaticId)))
            .andExpect(jsonPath("$.report.processing_type").value("SAME_DINING_APPROVED"))
            .andExpect(jsonPath("$.report.source_report_id").value(selectedId))
            .andExpect(jsonPath("$.report.reason").value("동일 코스 제보 승인에 따른 자동 처리"))
            .andExpect(jsonPath("$.report.processor").value(nullValue()))
            .andExpect(jsonPath("$.report.processed_at").value(nullValue()))
            .andExpect(jsonPath("$.report.reporter").doesNotExist());
        decide(automaticId, "reject", "U_OTHER").andExpect(status().isConflict());
        botDetail(rejectedId).andExpect(jsonPath("$.status").value("REJECTED"));
        botDetail(unrelatedId).andExpect(jsonPath("$.status").value("PENDING"));
        assertThat(storedDining().getSoldOut()).isNotNull();
        decide(selectedId, "approve", "U_OTHER").andExpect(status().isOk())
            .andExpect(jsonPath("$.already_processed").value(true))
            .andExpect(jsonPath("$.processing_id").value(group))
            .andExpect(jsonPath("$.report.processor.user_id").value("U_FIRST"))
            .andExpect(jsonPath("$.report.processor.display_name").value("U_FIRST"));
        decide(selectedId, "reject", "U_OTHER").andExpect(status().isConflict());
        decide(rejectedId, "approve", "U_OTHER").andExpect(status().isConflict());
        mockMvc.perform(get(BOT_PATH).header(BOT_HEADER, BOT_TOKEN).param("processing_id", group))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reports[*].report_id").value(contains(selectedId, automaticId)));
    }

    @Test
    void 품절해제후_새학생_제보는_과거_처리묶음과_분리한다() throws Exception {
        String key = UUID.randomUUID().toString();
        JsonNode created = body(submit(studentToken, dining, key, IMAGE_URL).andExpect(status().isCreated()));
        int selectedId = created.path("report_id").asInt();
        int automaticId = createReport(otherStudentToken, dining);
        String previousGroup = body(decide(selectedId, "approve", "U_FIRST").andExpect(status().isOk()))
            .path("processing_id").asText();
        submit(thirdStudentToken, dining, UUID.randomUUID().toString(), IMAGE_URL).andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("DINING_ALREADY_SOLD_OUT"));
        setSoldOut(false);
        int newId = createReport(thirdStudentToken, dining);
        assertThat(body(submit(studentToken, dining, key, IMAGE_URL).andExpect(status().isCreated())))
            .isEqualTo(created);
        decide(selectedId, "approve", "U_OTHER").andExpect(status().isOk())
            .andExpect(jsonPath("$.already_processed").value(true))
            .andExpect(jsonPath("$.processing_id").value(previousGroup))
            .andExpect(jsonPath("$.affected_report_ids").value(contains(selectedId, automaticId)));
        assertThat(storedDining().getSoldOut()).isNull();
        mockMvc.perform(get(BOT_PATH).header(BOT_HEADER, BOT_TOKEN)
                .param("dining_id", dining.getId().toString()).param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reports[*].report_id").value(contains(newId)))
            .andExpect(jsonPath("$.reports[0].processing_id").value(nullValue()))
            .andExpect(jsonPath("$.reports[0].reporter").doesNotExist());
        JsonNode processed = body(decide(newId, "approve", "U_OTHER").andExpect(status().isOk())
            .andExpect(jsonPath("$.affected_report_ids").value(contains(newId))));
        assertThat(processed.path("processing_id").asText()).isNotEqualTo(previousGroup);
        mockMvc.perform(get(BOT_PATH).header(BOT_HEADER, BOT_TOKEN).param("processing_id", previousGroup))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reports[*].report_id").value(contains(selectedId, automaticId)));
    }

    @Test
    void 영양사_선처리는_대기만_반려하고_해제후에도_재제보를_막는다() throws Exception {
        int rejectedId = createReport(studentToken, dining);
        int firstId = createReport(otherStudentToken, dining);
        int secondId = createReport(thirdStudentToken, dining);
        JsonNode rejected = body(decide(rejectedId, "reject", "U_FIRST").andExpect(status().isOk()));
        setSoldOut(true);
        assertThat(storedDining().getSoldOut()).isNotNull();
        for (int id : List.of(firstId, secondId)) {
            botDetail(id).andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.processing_type").value("COOP_PREPROCESSED"))
                .andExpect(jsonPath("$.reason").value("영양사 선처리로 제보 확인 없이 종료"));
        }
        botDetail(rejectedId).andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.processing_type").value("MANUAL"))
            .andExpect(jsonPath("$.processing_id").value(rejected.path("processing_id").asText()));
        setSoldOut(false);
        assertThat(storedDining().getSoldOut()).isNull();
        botDetail(firstId).andExpect(jsonPath("$.status").value("REJECTED"));
        submit(otherStudentToken, dining, UUID.randomUUID().toString(), IMAGE_URL).andExpect(status().isConflict());
    }

    @Test
    void 봇과_회원_인증을_분리하고_제보자는_관리자에게만_공개한다() throws Exception {
        int id = createReport(studentToken, dining);
        mockMvc.perform(get(BOT_PATH + "/{id}", id)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(BOT_PATH + "/{id}", id).header(BOT_HEADER, "wrong-token"))
            .andExpect(status().isUnauthorized());
        for (String token : List.of(studentToken, adminToken)) {
            mockMvc.perform(get(BOT_PATH + "/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
            mockMvc.perform(post(BOT_PATH + "/{id}/approve", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        }
        botDetail(id).andExpect(jsonPath("$.reporter").doesNotExist());
        mockMvc.perform(get(ADMIN_PATH + "/{id}", id).header(BOT_HEADER, BOT_TOKEN))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get(ADMIN_PATH + "/{id}", id).header(BOT_HEADER, BOT_TOKEN)
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isForbidden());
        mockMvc.perform(get(ADMIN_PATH + "/{id}", id).header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.reporter.id").value(student.getId()));
    }

    @Test
    void 관리자_목록은_최신순으로_미처리를_필터한다() throws Exception {
        int firstId = createReport(studentToken, dining);
        int rejectedId = createReport(otherStudentToken, dining);
        int latestId = createReport(studentToken, otherDining);
        decide(rejectedId, "reject", "U_FIRST").andExpect(status().isOk());
        mockMvc.perform(get(ADMIN_PATH).header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reports[*].report_id").value(contains(latestId, rejectedId, firstId)));
        mockMvc.perform(get(ADMIN_PATH).header("Authorization", "Bearer " + adminToken).param("only_pending", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reports[*].report_id").value(contains(latestId, firstId)));
    }

    @Test
    void 변경커서는_접수와_처리를_누락하거나_재시도에서_중복하지_않는다() throws Exception {
        changes("0").andExpect(jsonPath("$.changes").isEmpty())
            .andExpect(jsonPath("$.next_cursor").value("0"))
            .andExpect(jsonPath("$.has_more").value(false));
        int firstId = createReport(studentToken, dining);
        int secondId = createReport(otherStudentToken, dining);
        decide(firstId, "approve", "U_FIRST").andExpect(status().isOk());
        JsonNode first = body(changes("0").andExpect(jsonPath("$.has_more").value(true))
            .andExpect(jsonPath("$.changes[*].report_id").value(contains(firstId, secondId)))
            .andExpect(jsonPath("$.changes[*].event_type").value(contains("CREATED", "CREATED")))
            .andExpect(jsonPath("$.changes[*].status").value(contains("PENDING", "PENDING"))));
        String cursor = first.path("next_cursor").asText();
        assertThat(first.path("next_cursor").isTextual()).isTrue();
        JsonNode last = body(changes(cursor).andExpect(jsonPath("$.has_more").value(false))
            .andExpect(jsonPath("$.changes[*].report_id").value(contains(firstId, secondId)))
            .andExpect(jsonPath("$.changes[*].event_type").value(contains("PROCESSED", "PROCESSED")))
            .andExpect(jsonPath("$.changes[*].status").value(contains("APPROVED", "APPROVED"))));
        assertThat(body(changes("0")).path("changes")).isEqualTo(first.path("changes"));
        decide(firstId, "approve", "U_OTHER").andExpect(status().isOk());
        String lastCursor = last.path("next_cursor").asText();
        changes(lastCursor).andExpect(jsonPath("$.changes").isEmpty())
            .andExpect(jsonPath("$.next_cursor").value(lastCursor))
            .andExpect(jsonPath("$.has_more").value(false));
        String futureCursor = Long.toString(Long.parseLong(lastCursor) + 1);
        mockMvc.perform(get(BOT_PATH + "/changes").header(BOT_HEADER, BOT_TOKEN).param("cursor", futureCursor))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_CHANGE_CURSOR"));
    }

    @Test
    void Swagger는_공백시간과_봇_인증을_명세한다() throws Exception {
        JsonNode api = body(mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()));
        JsonNode schemas = api.at("/components/schemas");
        for (JsonNode time : List.of(schemas.at("/DiningReportCreateResponse/properties/created_at"),
            schemas.at("/DiningReportResponse/properties/created_at"),
            schemas.at("/DiningReportResponse/properties/processed_at"))) {
            assertThat(time.path("type").asText()).isEqualTo("string");
            assertThat(time.path("format").asText()).isNotEqualTo("date-time");
            assertThat(time.path("pattern").asText()).isNotBlank();
        }
        assertThat(schemas.at("/DiningReportResponse/properties/processed_at/nullable").asBoolean()).isTrue();
        JsonNode scheme = api.at("/components/securitySchemes/Bot Service Authentication");
        assertThat(scheme.path("type").asText()).isEqualTo("apiKey");
        assertThat(scheme.path("in").asText()).isEqualTo("header");
        assertThat(scheme.path("name").asText()).isEqualTo(BOT_HEADER);
        assertThat(api.path("paths").path(BOT_PATH).path("get").path("security"))
            .isNotEmpty().allSatisfy(requirement -> {
                assertThat(requirement.has("Bot Service Authentication")).isTrue();
                assertThat(requirement.has("Jwt Authentication")).isFalse();
            });
    }

    private ResultActions submit(String token, Dining target, String key, String imageUrl) throws Exception {
        var request = post("/dinings/{id}/soldout-reports", target.getId())
            .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("image_url", imageUrl)));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    private int createReport(String token, Dining target) throws Exception {
        return body(submit(token, target, UUID.randomUUID().toString(), IMAGE_URL)
            .andExpect(status().isCreated())).path("report_id").asInt();
    }

    private ResultActions decide(int id, String decision, String actor) throws Exception {
        return mockMvc.perform(post(BOT_PATH + "/{id}/{decision}", id, decision).header(BOT_HEADER, BOT_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("actor",
                Map.of("workspace_id", "T_DINING", "user_id", actor, "display_name", actor)))));
    }

    private ResultActions botDetail(int id) throws Exception {
        return mockMvc.perform(get(BOT_PATH + "/{id}", id).header(BOT_HEADER, BOT_TOKEN)).andExpect(status().isOk());
    }

    private ResultActions changes(String cursor) throws Exception {
        return mockMvc.perform(get(BOT_PATH + "/changes").header(BOT_HEADER, BOT_TOKEN)
            .param("cursor", cursor).param("limit", "2")).andExpect(status().isOk());
    }

    private void setSoldOut(boolean soldOut) throws Exception {
        mockMvc.perform(patch("/coop/dining/soldout").header("Authorization", "Bearer " + coopToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("menu_id", dining.getId(), "sold_out", soldOut))))
            .andExpect(status().isOk());
    }

    private JsonNode body(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsByteArray());
    }

    private Dining storedDining() {
        entityManager.flush();
        entityManager.clear();
        return diningRepository.getById(dining.getId());
    }
}
