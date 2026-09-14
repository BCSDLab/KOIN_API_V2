package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.ROLE_BASED;
import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_DUPLICATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CLOSED;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.team.recruitment.dto.CreateApplicationRequest;
import in.koreatech.koin.domain.team.recruitment.dto.UpdateApplicationStatusRequest;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRoleRepository;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentApplicationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.CustomException;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TeamRecruitmentApplicationConcurrencyTest extends AcceptanceTest {

    private static final int CONCURRENCY = 2;
    private static final long TIMEOUT_SECONDS = 10;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private TeamRecruitmentApplicationService applicationService;

    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;

    @Autowired
    private TeamRecruitmentRoleRepository roleRepository;

    @Autowired
    private TeamRecruitmentProfileRepository profileRepository;

    @Autowired
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Autowired
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Autowired
    private TeamRecruitmentChatMemberRepository chatMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private Integer recruitmentId;
    private Integer roleId;
    private Integer teamRoomId;
    private Integer authorId;
    private Integer firstApplicantId;
    private Integer secondApplicantId;

    @BeforeEach
    void setUp() {
        clear();
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.executeWithoutResult(status -> saveRoleBasedRecruitment());
    }

    @AfterEach
    void tearDown() {
        clear();
    }

    @Test
    void 같은_지원자가_동시에_지원해도_지원서와_알림은_한_번만_생성된다() throws Exception {
        List<Attempt> attempts = runConcurrently(
            () -> applicationService.createApplication(
                new CreateApplicationRequest(roleId, "첫 번째 동시 지원", "월수금 저녁"),
                recruitmentId,
                firstApplicantId
            ),
            () -> applicationService.createApplication(
                new CreateApplicationRequest(roleId, "두 번째 동시 지원", "화목 저녁"),
                recruitmentId,
                firstApplicantId
            )
        );

        assertThat(attempts).filteredOn(Attempt::isSuccess).hasSize(1);
        assertThat(attempts).filteredOn(attempt -> !attempt.isSuccess())
            .singleElement()
            .satisfies(attempt -> assertThat(attempt.failure())
                .isInstanceOfSatisfying(CustomException.class, exception -> assertThat(exception.getErrorCode())
                    .isEqualTo(TEAM_RECRUITMENT_APPLICATION_DUPLICATE)));

        transactionTemplate.executeWithoutResult(status -> {
            assertThat(count("SELECT COUNT(*) FROM team_recruitment_application WHERE recruitment_id = ?",
                recruitmentId)).isOne();
            assertThat(count("""
                SELECT COUNT(*)
                FROM team_recruitment_application
                WHERE recruitment_id = ? AND applicant_id = ? AND role_id = ? AND status = 'PENDING'
                """, recruitmentId, firstApplicantId, roleId)).isOne();
            Integer applicationId = jdbcTemplate.queryForObject("""
                SELECT id
                FROM team_recruitment_application
                WHERE recruitment_id = ? AND applicant_id = ?
                """, Integer.class, recruitmentId, firstApplicantId);
            assertThat(count("""
                SELECT COUNT(*)
                FROM team_recruitment_notification
                WHERE recruitment_id = ? AND recipient_id = ? AND application_id = ?
                    AND type = 'NEW_APPLICATION' AND target_type = 'APPLICANT_MANAGEMENT'
                """, recruitmentId, authorId, applicationId)).isOne();
            assertThat(countLinkedNotificationOutbox()).isOne();
            assertThat(count("SELECT COUNT(*) FROM team_recruitment_chat_member WHERE chat_room_id = ?",
                teamRoomId)).isOne();
        });
    }

    @Test
    void 마지막_한_자리를_동시에_승인해도_한_명만_승인되고_중복_후속_작업이_생기지_않는다() throws Exception {
        ApplicationIds applications = transactionTemplate.execute(status -> savePendingApplications());

        List<Attempt> attempts = runConcurrently(
            () -> {
                applicationService.updateApplicationStatus(
                    new UpdateApplicationStatusRequest(TeamRecruitmentApplicationStatus.ACCEPTED),
                    recruitmentId,
                    applications.first(),
                    authorId
                );
                return null;
            },
            () -> {
                applicationService.updateApplicationStatus(
                    new UpdateApplicationStatusRequest(TeamRecruitmentApplicationStatus.ACCEPTED),
                    recruitmentId,
                    applications.second(),
                    authorId
                );
                return null;
            }
        );

        assertThat(attempts).filteredOn(Attempt::isSuccess).hasSize(1);
        assertThat(attempts).filteredOn(attempt -> !attempt.isSuccess())
            .singleElement()
            .satisfies(attempt -> assertThat(attempt.failure())
                .isInstanceOfSatisfying(CustomException.class, exception -> assertThat(exception.getErrorCode())
                    .isEqualTo(TEAM_RECRUITMENT_CLOSED)));

        transactionTemplate.executeWithoutResult(status -> {
            assertThat(jdbcTemplate.queryForList("""
                SELECT status
                FROM team_recruitment_application
                WHERE recruitment_id = ?
                """, String.class, recruitmentId))
                .containsExactlyInAnyOrder("ACCEPTED", "REJECTED");
            assertThat(count("""
                SELECT COUNT(*)
                FROM team_recruitment
                WHERE id = ? AND status = 'CLOSED' AND current_participants = 1 AND max_participants = 1
                """, recruitmentId)).isOne();
            assertThat(count("""
                SELECT COUNT(*)
                FROM team_recruitment_role
                WHERE id = ? AND current_participants = 1 AND max_participants = 1
                """, roleId)).isOne();

            Integer acceptedApplicantId = jdbcTemplate.queryForObject("""
                SELECT applicant_id
                FROM team_recruitment_application
                WHERE recruitment_id = ? AND status = 'ACCEPTED'
                """, Integer.class, recruitmentId);
            assertThat(jdbcTemplate.queryForList("""
                SELECT user_id
                FROM team_recruitment_chat_member
                WHERE chat_room_id = ?
                """, Integer.class, teamRoomId))
                .containsExactlyInAnyOrder(authorId, acceptedApplicantId);

            assertThat(jdbcTemplate.queryForList("""
                SELECT type
                FROM team_recruitment_notification
                WHERE recruitment_id = ?
                """, String.class, recruitmentId))
                .containsExactlyInAnyOrder(
                    "APPLICATION_ACCEPTED",
                    "APPLICATION_REJECTED",
                    "RECRUITMENT_CLOSED"
                );
            assertThat(jdbcTemplate.queryForList("""
                SELECT SUBSTRING_INDEX(event_key, ':', -1)
                FROM team_recruitment_outbox_event
                WHERE aggregate_id = ?
                """, String.class, recruitmentId))
                .containsExactlyInAnyOrder(
                    "APPLICATION_ACCEPTED",
                    "APPLICATION_REJECTED",
                    "RECRUITMENT_CLOSED"
                );
            assertThat(countLinkedNotificationOutbox()).isEqualTo(3);
        });
    }

    private void saveRoleBasedRecruitment() {
        Department department = departmentFixture.컴퓨터공학부();
        Student author = userFixture.준호_학생(department, null);
        Student firstApplicant = userFixture.성빈_학생(department);
        Student secondApplicant = saveSecondApplicant(department);
        saveProfile(firstApplicant, "첫 지원자");
        saveProfile(secondApplicant, "두 번째 지원자");

        TeamRecruitment recruitment = recruitmentRepository.save(TeamRecruitment.builder()
            .author(author.getUser())
            .category(PROJECT)
            .title("동시성 검증 모집")
            .meetingType(ONLINE)
            .activityStartDate(LocalDate.now(clock).plusDays(2))
            .activityEndDate(LocalDate.now(clock).plusDays(10))
            .deadlineDate(LocalDate.now(clock).plusDays(1))
            .recruitmentType(ROLE_BASED)
            .maxParticipants(1)
            .currentParticipants(0)
            .description("한 명을 모집합니다.")
            .status(RECRUITING)
            .build());
        TeamRecruitmentRole role = roleRepository.save(TeamRecruitmentRole.builder()
            .recruitment(recruitment)
            .name("백엔드")
            .maxParticipants(1)
            .currentParticipants(0)
            .displayOrder(1)
            .build());
        TeamRecruitmentChatRoom teamRoom = chatRoomRepository.save(TeamRecruitmentChatRoom.builder()
            .recruitment(recruitment)
            .roomScopeKey("TEAM")
            .roomType(TEAM)
            .status(ACTIVE)
            .build());
        chatMemberRepository.save(TeamRecruitmentChatMember.builder()
            .chatRoom(teamRoom)
            .user(author.getUser())
            .build());

        recruitmentId = recruitment.getId();
        roleId = role.getId();
        teamRoomId = teamRoom.getId();
        authorId = author.getUser().getId();
        firstApplicantId = firstApplicant.getUser().getId();
        secondApplicantId = secondApplicant.getUser().getId();
    }

    private void saveProfile(Student applicant, String nickname) {
        profileRepository.save(TeamRecruitmentProfile.builder()
            .user(applicant.getUser())
            .profileNickname(nickname)
            .preferredRole("백엔드")
            .selfIntroduction("동시성 테스트 지원자")
            .build());
    }

    private Student saveSecondApplicant(Department department) {
        return studentRepository.save(Student.builder()
            .studentNumber("2023999999")
            .department(department)
            .userIdentity(UNDERGRADUATE)
            .isGraduated(false)
            .user(User.builder()
                .loginPw("test-password")
                .name("두 번째 동시성 지원자")
                .anonymousNickname("익명_동시성지원자")
                .userType(STUDENT)
                .isAuthed(true)
                .isDeleted(false)
                .build())
            .build());
    }

    private ApplicationIds savePendingApplications() {
        TeamRecruitment recruitment = recruitmentRepository.findById(recruitmentId).orElseThrow();
        TeamRecruitmentRole role = roleRepository.findById(roleId).orElseThrow();
        TeamRecruitmentApplication first = applicationRepository.save(pendingApplication(
            recruitment,
            role,
            firstApplicantId,
            "첫 지원서"
        ));
        TeamRecruitmentApplication second = applicationRepository.save(pendingApplication(
            recruitment,
            role,
            secondApplicantId,
            "두 번째 지원서"
        ));
        return new ApplicationIds(first.getId(), second.getId());
    }

    private TeamRecruitmentApplication pendingApplication(
        TeamRecruitment recruitment,
        TeamRecruitmentRole role,
        Integer applicantId,
        String motivation
    ) {
        return TeamRecruitmentApplication.builder()
            .recruitment(recruitment)
            .applicant(userRepository.getById(applicantId))
            .role(role)
            .motivation(motivation)
            .availability("평일 저녁")
            .status(PENDING)
            .profileSnapshot("""
                {
                  "nickname": "지원자",
                  "department": "컴퓨터공학부",
                  "student_year": 2023,
                  "preferred_role": "백엔드",
                  "skills": [],
                  "activities": [],
                  "self_introduction": "소개"
                }
                """)
            .snapshotVersion(1)
            .build();
    }

    private List<Attempt> runConcurrently(Callable<?> first, Callable<?> second) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        CountDownLatch ready = new CountDownLatch(CONCURRENCY);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Attempt> firstFuture = executor.submit(awaitStart(ready, start, first));
            Future<Attempt> secondFuture = executor.submit(awaitStart(ready, start, second));
            assertThat(ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(
                firstFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                secondFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            );
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private Callable<Attempt> awaitStart(CountDownLatch ready, CountDownLatch start, Callable<?> action) {
        return () -> {
            ready.countDown();
            if (!start.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("동시 실행 시작 신호를 기다리다 시간 초과했습니다.");
            }
            try {
                action.call();
                return Attempt.success();
            } catch (Throwable failure) {
                return Attempt.failure(failure);
            }
        };
    }

    private long count(String sql, Object... arguments) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class, arguments);
        return result == null ? 0 : result;
    }

    private long countLinkedNotificationOutbox() {
        return count("""
            SELECT COUNT(*)
            FROM team_recruitment_notification notification
            JOIN team_recruitment_outbox_event outbox
                ON outbox.event_key = CONCAT(
                    'team-recruitment:application:', notification.application_id, ':', notification.type
                )
            WHERE notification.recruitment_id = ?
                AND outbox.aggregate_id = notification.recruitment_id
                AND outbox.event_type = 'TEAM_RECRUITMENT_NOTIFICATION'
                AND outbox.aggregate_type = 'TEAM_RECRUITMENT'
                AND JSON_UNQUOTE(JSON_EXTRACT(outbox.payload, '$.type')) = notification.type
                AND JSON_UNQUOTE(JSON_EXTRACT(outbox.payload, '$.target_type')) = notification.target_type
                AND CAST(JSON_UNQUOTE(JSON_EXTRACT(outbox.payload, '$.recipient_id')) AS UNSIGNED)
                    = notification.recipient_id
                AND CAST(JSON_UNQUOTE(JSON_EXTRACT(outbox.payload, '$.recruitment_id')) AS UNSIGNED)
                    = notification.recruitment_id
                AND CAST(JSON_UNQUOTE(JSON_EXTRACT(outbox.payload, '$.application_id')) AS UNSIGNED)
                    = notification.application_id
                AND CAST(JSON_UNQUOTE(JSON_EXTRACT(outbox.payload, '$.notification_id')) AS UNSIGNED)
                    = notification.id
                AND JSON_UNQUOTE(JSON_EXTRACT(outbox.payload, '$.message_preview'))
                    = notification.message_preview
                AND (
                    (notification.chat_room_id IS NULL
                        AND JSON_TYPE(JSON_EXTRACT(outbox.payload, '$.chat_room_id')) = 'NULL')
                    OR CAST(JSON_UNQUOTE(JSON_EXTRACT(outbox.payload, '$.chat_room_id')) AS UNSIGNED)
                        = notification.chat_room_id
                )
            """, recruitmentId);
    }

    private record ApplicationIds(Integer first, Integer second) {
    }

    private record Attempt(Throwable failure) {

        static Attempt success() {
            return new Attempt(null);
        }

        static Attempt failure(Throwable failure) {
            return new Attempt(failure);
        }

        boolean isSuccess() {
            return failure == null;
        }
    }
}
