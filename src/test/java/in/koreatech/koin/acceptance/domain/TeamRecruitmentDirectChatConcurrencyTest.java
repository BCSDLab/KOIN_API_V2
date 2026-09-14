package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.DIRECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;

@Import(TeamRecruitmentDirectChatConcurrencyTest.DirectRoomSaveBarrier.class)
class TeamRecruitmentDirectChatConcurrencyTest extends AcceptanceTest {

    private static final int CONCURRENT_REQUEST_COUNT = 8;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;

    @Autowired
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Autowired
    private DirectRoomSaveBarrier directRoomSaveBarrier;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private TeamRecruitment recruitment;
    private TeamRecruitmentApplication application;
    private String authorToken;

    @BeforeEach
    void setUp() {
        clear();
        Department department = departmentFixture.컴퓨터공학부();
        Student author = userFixture.준호_학생(department, null);
        Student applicant = userFixture.성빈_학생(department);
        authorToken = userFixture.getToken(author.getUser());

        recruitment = recruitmentRepository.save(TeamRecruitment.builder()
            .author(author.getUser())
            .category(PROJECT)
            .title("동시성 테스트 모집글")
            .meetingType(ONLINE)
            .activityStartDate(LocalDate.now(clock).plusDays(2))
            .activityEndDate(LocalDate.now(clock).plusDays(10))
            .deadlineDate(LocalDate.now(clock).plusDays(1))
            .recruitmentType(GENERAL)
            .maxParticipants(2)
            .currentParticipants(1)
            .description("동일한 지원서로 DIRECT 채팅방을 동시에 생성한다.")
            .status(RECRUITING)
            .build());
        application = applicationRepository.save(TeamRecruitmentApplication.builder()
            .recruitment(recruitment)
            .applicant(applicant.getUser())
            .motivation("지원 동기")
            .availability("월수금 20시 이후")
            .status(ACCEPTED)
            .profileSnapshot("{}")
            .snapshotVersion(1)
            .build());
    }

    @Test
    void 동일한_지원서의_DIRECT_채팅방_생성_요청_2건은_하나의_방으로_수렴한다() throws Exception {
        assertConcurrentGetOrCreate(2);
    }

    @Test
    void 동일한_지원서의_DIRECT_채팅방_생성_요청_N건은_하나의_방으로_수렴한다() throws Exception {
        assertConcurrentGetOrCreate(CONCURRENT_REQUEST_COUNT);
    }

    private void assertConcurrentGetOrCreate(int requestCount) throws Exception {
        directRoomSaveBarrier.expectInsertions(requestCount);

        entityManager.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        List<MvcResult> results = performConcurrentRequests(requestCount);
        List<Integer> statuses = results.stream()
            .map(result -> result.getResponse().getStatus())
            .toList();
        List<Integer> chatRoomIds = new ArrayList<>();
        for (MvcResult result : results) {
            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            chatRoomIds.add(response.path("chat_room_id").asInt());
        }

        assertThat(statuses).containsOnly(200, 201);
        assertThat(statuses).containsExactlyInAnyOrderElementsOf(expectedStatuses(requestCount));
        assertThat(chatRoomIds).doesNotContain(0).allMatch(chatRoomIds.get(0)::equals);

        Integer chatRoomCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM team_recruitment_chat_room
            WHERE recruitment_id = ?
              AND application_id = ?
              AND room_type = 'DIRECT'
            """, Integer.class, recruitment.getId(), application.getId());
        Integer memberCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM team_recruitment_chat_member
            WHERE chat_room_id = ?
            """, Integer.class, chatRoomIds.get(0));

        assertThat(chatRoomCount).isOne();
        assertThat(memberCount).isEqualTo(2);
    }

    private List<MvcResult> performConcurrentRequests(int requestCount) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<MvcResult>> futures = new ArrayList<>();
            for (int i = 0; i < requestCount; i++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, SECONDS)) {
                        throw new IllegalStateException("동시 요청 시작 대기 시간이 초과되었습니다.");
                    }
                    return mockMvc.perform(post(
                            "/chatroom/team-recruitment/{recruitmentId}/applications/{applicationId}/direct",
                            recruitment.getId(), application.getId())
                        .header("Authorization", "Bearer " + authorToken))
                        .andReturn();
                }));
            }
            assertThat(ready.await(5, SECONDS)).isTrue();
            start.countDown();

            List<MvcResult> results = new ArrayList<>();
            for (Future<MvcResult> future : futures) {
                results.add(future.get(15, SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, SECONDS)).isTrue();
        }
    }

    private List<Integer> expectedStatuses(int requestCount) {
        List<Integer> statuses = new ArrayList<>();
        statuses.add(201);
        for (int i = 1; i < requestCount; i++) {
            statuses.add(200);
        }
        return statuses;
    }

    @Aspect
    static class DirectRoomSaveBarrier {

        private final AtomicReference<CountDownLatch> insertions =
            new AtomicReference<>(new CountDownLatch(0));

        void expectInsertions(int requestCount) {
            insertions.set(new CountDownLatch(requestCount));
        }

        @Around("bean(teamRecruitmentChatRoomRepository) && execution(* save(..)) && args(chatRoom)")
        Object awaitConcurrentInsertions(
            ProceedingJoinPoint joinPoint,
            TeamRecruitmentChatRoom chatRoom
        ) throws Throwable {
            if (chatRoom.getRoomType() == DIRECT) {
                CountDownLatch latch = insertions.get();
                latch.countDown();
                // 수정 전에는 모든 요청이 insert까지 도달하고, 수정 후에는 최초 요청만 도달한다.
                latch.await(1, SECONDS);
            }
            return joinPoint.proceed();
        }
    }
}
