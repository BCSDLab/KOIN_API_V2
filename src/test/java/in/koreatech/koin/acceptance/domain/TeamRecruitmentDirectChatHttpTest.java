package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserIdentity;
import in.koreatech.koin.domain.user.model.UserType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;

/** Checks both participants through real HTTP authentication, chat services and databases. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "server.address=127.0.0.1",
    "spring.jpa.properties.hibernate.show_sql=false",
    "logging.level.org.springframework.transaction=INFO",
    "logging.level.org.springframework.orm.jpa.JpaTransactionManager=INFO",
    "logging.level.org.hibernate.engine.transaction.internal=INFO",
    "logging.level.org.hibernate.tool.schema=OFF",
    "logging.level.in.koreatech.koin.global.exception.GlobalExceptionHandler=OFF"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TeamRecruitmentDirectChatHttpTest extends AcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserAcceptanceFixture userFixture;
    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;
    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;
    @Autowired
    private TeamRecruitmentProfileRepository profileRepository;
    @Autowired
    private TeamRecruitmentChatMemberRepository memberRepository;
    @Autowired
    private TeamRecruitmentChatRoomRepository chatRoomRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper mapper;

    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10)).build();
    private final List<Map<String, Object>> evidence = new ArrayList<>();

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void bothParticipantsCanCreateAndReuseDirectChatThroughRealHttp(boolean applicantCreatesFirst) throws Exception {
        evidence.clear();
        clear();
        Department department = departmentFixture.컴퓨터공학부();
        Student author = userFixture.준호_학생(department, null);
        Student applicant = userFixture.성빈_학생(department);
        Student outsider = studentRepository.save(Student.builder()
            .studentNumber("2026136999").department(department)
            .userIdentity(UserIdentity.UNDERGRADUATE).isGraduated(false)
            .user(User.builder().loginId("httpoutsider")
                .loginPw(passwordEncoder.encode("1234"))
                .name("HTTP 제3자").nickname("HTTP제3자").anonymousNickname("익명_HTTP제3자")
                .phoneNumber("01000000999").email("httpoutsider@example.com")
                .userType(UserType.STUDENT).isAuthed(true).isDeleted(false).build()).build());
        profileRepository.save(TeamRecruitmentProfile.builder()
            .user(applicant.getUser()).profileNickname("지원자")
            .preferredRole("백엔드").selfIntroduction("로컬 HTTP 재현 계정").build());
        LocalDate today = LocalDate.now(clock);
        TeamRecruitment recruitment = recruitmentRepository.save(TeamRecruitment.builder()
            .author(author.getUser()).category(PROJECT).title("DIRECT 권한 HTTP 재현")
            .meetingType(ONLINE).activityStartDate(today.plusDays(2))
            .activityEndDate(today.plusDays(10)).deadlineDate(today.plusDays(1))
            .recruitmentType(GENERAL).maxParticipants(3).currentParticipants(0)
            .description("로컬 재현 전용 데이터").status(RECRUITING).build());
        int recruitmentId = recruitment.getId();
        int authorId = author.getUser().getId();
        int applicantId = applicant.getUser().getId();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        try {
            String authorToken = login("author", "juno");
            String applicantToken = login("applicant", "testsungbeen");
            String outsiderToken = login("outsider", outsider.getUser().getLoginId());

            JsonNode me = expect("applicant authentication", "applicant", "GET", "/user/student/me",
                applicantToken, null, 200, null);
            assertThat(me.path("id").asInt()).isEqualTo(applicantId);
            JsonNode profile = expect("latest profile endpoint", "applicant", "GET", "/v3/users/me",
                applicantToken, null, 200, null);
            assertThat(profile.path("id").asInt()).isEqualTo(applicantId);

            JsonNode application = expect("apply", "applicant", "POST",
                "/team-recruitments/" + recruitmentId + "/applications", applicantToken,
                "{\"role_id\":null,\"motivation\":\"지원 동기\",\"availability\":\"평일 저녁\"}", 201, null);
            int applicationId = application.path("application_id").asInt();
            assertThat(applicationId).isPositive();
            String directPath = "/chatroom/team-recruitment/" + recruitmentId
                + "/applications/" + applicationId + "/direct";

            expect("anonymous control", "anonymous", "POST", directPath, null, null,
                401, "UNAUTHORIZED_USER");
            expect("pending applicant", "applicant", "POST", directPath, applicantToken, null,
                409, "TEAM_RECRUITMENT_APPLICATION_NOT_ACCEPTED");
            expect("pending author", "author", "POST", directPath, authorToken, null,
                409, "TEAM_RECRUITMENT_APPLICATION_NOT_ACCEPTED");
            expect("accept application", "author", "PUT",
                "/team-recruitments/" + recruitmentId + "/applications/" + applicationId + "/status",
                authorToken, "{\"status\":\"ACCEPTED\"}", 204, null);
            JsonNode direct = expect("first participant creates room", applicantCreatesFirst ? "applicant" : "author",
                "POST", directPath, applicantCreatesFirst ? applicantToken : authorToken, null, 201, null);
            int chatRoomId = direct.path("chat_room_id").asInt();
            assertThat(chatRoomId).isPositive();
            assertThat(direct.path("counterpart").path("id").asInt())
                .isEqualTo(applicantCreatesFirst ? authorId : applicantId);
            JsonNode authorRoom = expect("author reuses room", "author", "POST", directPath,
                authorToken, null, 200, null);
            assertThat(authorRoom.path("chat_room_id").asInt()).isEqualTo(chatRoomId);
            assertThat(authorRoom.path("counterpart").path("id").asInt()).isEqualTo(applicantId);
            JsonNode applicantRoom = expect("applicant reuses room", "applicant", "POST", directPath,
                applicantToken, null, 200, null);
            assertThat(applicantRoom.path("chat_room_id").asInt()).isEqualTo(chatRoomId);
            assertThat(applicantRoom.path("counterpart").path("id").asInt()).isEqualTo(authorId);
            assertThat(memberRepository.findAllWithUsersByChatRoomIds(List.of(chatRoomId)))
                .extracting(member -> member.getUser().getId()).containsExactlyInAnyOrder(authorId, applicantId);
            assertThat(chatRoomRepository.findAllByRecruitment_Id(recruitmentId))
                .filteredOn(room -> room.getRoomType() == TeamRecruitmentChatRoomType.DIRECT).hasSize(1);
            var simultaneousDirect = client.sendAsync(
                request("POST", directPath, applicantToken, null), HttpResponse.BodyHandlers.ofString());
            var simultaneousMe = client.sendAsync(
                request("GET", "/user/student/me", applicantToken, null), HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> simultaneousDirectResponse = simultaneousDirect.join();
            HttpResponse<String> simultaneousMeResponse = simultaneousMe.join();
            record("simultaneous direct request", "applicant", "POST", directPath,
                simultaneousDirectResponse, true);
            record("simultaneous profile request", "applicant", "GET", "/user/student/me",
                simultaneousMeResponse, true);
            assertThat(simultaneousDirectResponse.statusCode()).isEqualTo(200);
            assertThat(mapper.readTree(simultaneousDirectResponse.body()).path("counterpart").path("id").asInt())
                .isEqualTo(authorId);
            assertThat(simultaneousMeResponse.statusCode()).isEqualTo(200);
            assertThat(mapper.readTree(simultaneousMeResponse.body()).path("id").asInt()).isEqualTo(applicantId);
            expect("outsider control", "outsider", "POST", directPath, outsiderToken, null,
                403, "TEAM_RECRUITMENT_FORBIDDEN");

            JsonNode applications = expect("applicant receives room id", "applicant", "GET",
                "/team-recruitments/me/applications", applicantToken, null, 200, null);
            assertThat(applications.path("applications").get(0).path("direct_chat_room_id").asInt())
                .isEqualTo(chatRoomId);
            String roomPath = "/chatroom/team-recruitment/" + recruitmentId + "/" + chatRoomId;
            expect("applicant enters existing room", "applicant", "GET", roomPath,
                applicantToken, null, 200, null);
            expect("outsider cannot enter existing room", "outsider", "GET", roomPath,
                outsiderToken, null, 403, "TEAM_RECRUITMENT_CHAT_FORBIDDEN");
            expect("applicant sends message", "applicant", "POST", roomPath + "/messages",
                applicantToken, "{\"content\":\"로컬 재현 메시지\",\"is_image\":false}", 200, null);
            JsonNode messages = expect("author reads applicant message", "author", "GET",
                roomPath + "/messages", authorToken, null, 200, null);
            assertThat(messages.toString()).contains("로컬 재현 메시지");
        } finally {
            Path output = Path.of("outputs/team-chat-fix-20261005/"
                + (applicantCreatesFirst ? "applicant-first-results.json" : "author-first-results.json"));
            Files.createDirectories(output.getParent());
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("source_sha", "6d7c3bdd7a96b4a77a705223224c55e10dd00a33");
            report.put("local_patch", "authorize recruitment author or application applicant");
            report.put("first_caller", applicantCreatesFirst ? "applicant" : "author");
            report.put("server", "http://127.0.0.1:" + port);
            report.put("transport", "java.net.http.HttpClient -> real embedded Tomcat");
            report.put("database", "isolated Testcontainers MySQL / Redis / MongoDB");
            report.put("requests", evidence);
            Files.writeString(output, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report));
            System.out.println("HTTP_REPRODUCTION_REPORT=" + output.toAbsolutePath());
        }
    }

    private String login(String actor, String loginId) throws Exception {
        HttpResponse<String> response = send("POST", "/v2/users/login", null,
            "{\"login_id\":\"" + loginId + "\",\"login_pw\":\"1234\"}");
        record("real login", actor, "POST", "/v2/users/login", response, false);
        assertThat(response.statusCode()).isEqualTo(201);
        JsonNode json = mapper.readTree(response.body());
        assertThat(json.path("user_type").asText()).isEqualTo("STUDENT");
        assertThat(json.path("token").asText()).isNotBlank();
        return json.path("token").asText();
    }

    private JsonNode expect(String scenario, String actor, String method, String path,
        String token, String body, int expectedStatus, String expectedCode) throws Exception {
        HttpResponse<String> response = send(method, path, token, body);
        record(scenario, actor, method, path, response, true);
        assertThat(response.statusCode()).as(scenario + ": " + response.body()).isEqualTo(expectedStatus);
        JsonNode json = response.body().isBlank() ? mapper.createObjectNode() : mapper.readTree(response.body());
        if (expectedCode != null) {
            assertThat(json.path("code").asText()).as(scenario).isEqualTo(expectedCode);
        }
        return json;
    }

    private HttpResponse<String> send(String method, String path, String token, String body) throws Exception {
        return client.send(request(method, path, token, body), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest request(String method, String path, String token, String body) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .timeout(Duration.ofSeconds(20))
            .header("User-Agent", "Koin-Local-Reproduction Android");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body));
        return request.build();
    }

    private void record(String scenario, String actor, String method, String path,
        HttpResponse<String> response, boolean includeError) throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("scenario", scenario);
        row.put("actor", actor);
        row.put("method", method);
        row.put("path", path);
        row.put("status", response.statusCode());
        if (includeError && !response.body().isBlank()) {
            JsonNode json = mapper.readTree(response.body());
            for (String field : List.of("code", "message", "errorTraceId", "chat_room_id", "room_type")) {
                if (json.has(field)) {
                    row.put(field, json.get(field));
                }
            }
        }
        evidence.add(row);
        System.out.println("HTTP_REPRODUCTION " + mapper.writeValueAsString(row));
    }
}
