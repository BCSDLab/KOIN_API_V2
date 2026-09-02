package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.READ_ONLY;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.DIRECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.MY_APPLICATIONS;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static org.assertj.core.api.Assertions.assertThat;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentDeadlineCloseCoordinator;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentDeadlineScheduler;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

class TeamRecruitmentDeadlineCloseIntegrationTest extends AcceptanceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TeamRecruitmentDeadlineCloseCoordinator coordinator;

    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;

    @Autowired
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Autowired
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Autowired
    private TeamRecruitmentNotificationRepository notificationRepository;

    @Autowired
    private TeamRecruitmentOutboxEventRepository outboxEventRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private TeamRecruitmentDeadlineScheduler deadlineScheduler;

    @AfterEach
    void cleanUp() {
        clear();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 손상된_모집의_변경은_전부_롤백되고_다음_정상_모집은_마감된다() {
        clear();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        LocalDate today = LocalDate.now(clock.withZone(KST));
        Scenario scenario = transactionTemplate.execute(status -> seed(today));

        coordinator.closeExpiredRecruitments();

        transactionTemplate.executeWithoutResult(status -> assertResults(scenario));
    }

    private Scenario seed(LocalDate today) {
        User author = userRepository.save(user());
        User failedAcceptedApplicant = userRepository.save(user());
        User failedPendingApplicant = userRepository.save(user());
        User successfulPendingApplicant = userRepository.save(user());

        TeamRecruitment failedRecruitment = recruitment(author, today, "손상 모집", 1);
        TeamRecruitment successfulRecruitment = recruitment(author, today, "정상 모집", 0);
        recruitmentRepository.save(failedRecruitment);
        recruitmentRepository.save(successfulRecruitment);

        TeamRecruitmentApplication failedAccepted = application(
            failedRecruitment,
            failedAcceptedApplicant,
            ACCEPTED
        );
        TeamRecruitmentApplication failedPending = application(
            failedRecruitment,
            failedPendingApplicant,
            PENDING
        );
        TeamRecruitmentApplication successfulPending = application(
            successfulRecruitment,
            successfulPendingApplicant,
            PENDING
        );
        applicationRepository.save(failedAccepted);
        applicationRepository.save(failedPending);
        applicationRepository.save(successfulPending);

        TeamRecruitmentChatRoom failedDirectRoom = chatRoom(
            failedRecruitment,
            failedPending,
            DIRECT,
            "APPLICATION:" + failedPending.getId()
        );
        TeamRecruitmentChatRoom successfulTeamRoom = chatRoom(
            successfulRecruitment,
            null,
            TEAM,
            "TEAM"
        );
        chatRoomRepository.save(failedDirectRoom);
        chatRoomRepository.save(successfulTeamRoom);

        return new Scenario(
            failedRecruitment.getId(),
            failedAccepted.getId(),
            failedPending.getId(),
            failedDirectRoom.getId(),
            successfulRecruitment.getId(),
            successfulPending.getId(),
            successfulPendingApplicant.getId(),
            successfulTeamRoom.getId()
        );
    }

    private void assertResults(Scenario scenario) {
        TeamRecruitment failedRecruitment = recruitmentRepository.findById(scenario.failedRecruitmentId())
            .orElseThrow();
        TeamRecruitmentApplication failedAccepted = applicationRepository.findById(scenario.failedAcceptedId())
            .orElseThrow();
        TeamRecruitmentApplication failedPending = applicationRepository.findById(scenario.failedPendingId())
            .orElseThrow();
        TeamRecruitmentChatRoom failedDirectRoom = chatRoomRepository.findById(scenario.failedDirectRoomId())
            .orElseThrow();

        assertThat(failedRecruitment.getStatus()).isEqualTo(RECRUITING);
        assertThat(failedAccepted.getStatus()).isEqualTo(ACCEPTED);
        assertThat(failedPending.getStatus()).isEqualTo(PENDING);
        assertThat(failedPending.getDecisionReason()).isNull();
        assertThat(failedDirectRoom.getStatus()).isEqualTo(ACTIVE);
        assertThat(outboxEventRepository.findByEventKey(rejectionEventKey(scenario.failedPendingId())))
            .isEmpty();
        assertThat(notificationRepository.findForOutbox(
            failedPending.getApplicant().getId(),
            APPLICATION_REJECTED,
            MY_APPLICATIONS,
            scenario.failedRecruitmentId(),
            scenario.failedPendingId(),
            null
        )).isEmpty();

        TeamRecruitment successfulRecruitment = recruitmentRepository.findById(scenario.successfulRecruitmentId())
            .orElseThrow();
        TeamRecruitmentApplication successfulPending = applicationRepository.findById(
            scenario.successfulPendingId()
        ).orElseThrow();
        TeamRecruitmentChatRoom successfulTeamRoom = chatRoomRepository.findById(scenario.successfulTeamRoomId())
            .orElseThrow();

        assertThat(successfulRecruitment.getStatus()).isEqualTo(CLOSED);
        assertThat(successfulPending.getStatus()).isEqualTo(REJECTED);
        assertThat(successfulPending.getDecisionReason()).isEqualTo("RECRUITMENT_CLOSED");
        assertThat(successfulTeamRoom.getStatus()).isEqualTo(READ_ONLY);
        assertThat(notificationRepository.findForOutbox(
            scenario.successfulApplicantId(),
            APPLICATION_REJECTED,
            MY_APPLICATIONS,
            scenario.successfulRecruitmentId(),
            scenario.successfulPendingId(),
            null
        )).hasSize(1);
        TeamRecruitmentOutboxEvent successfulOutbox = outboxEventRepository
            .findByEventKey(rejectionEventKey(scenario.successfulPendingId()))
            .orElseThrow();
        assertThat(successfulOutbox.getStatus()).isEqualTo(TeamRecruitmentOutboxEventStatus.PENDING);
    }

    private User user() {
        return User.builder()
            .loginPw("password")
            .userType(UserType.STUDENT)
            .isAuthed(true)
            .isDeleted(false)
            .build();
    }

    private TeamRecruitment recruitment(
        User author,
        LocalDate today,
        String title,
        int currentParticipants
    ) {
        return TeamRecruitment.builder()
            .author(author)
            .category(PROJECT)
            .title(title)
            .meetingType(ONLINE)
            .activityStartDate(today.plusDays(1))
            .activityEndDate(today.plusDays(2))
            .deadlineDate(today.minusDays(1))
            .recruitmentType(GENERAL)
            .maxParticipants(5)
            .currentParticipants(currentParticipants)
            .description("마감 트랜잭션 격리 테스트")
            .status(RECRUITING)
            .build();
    }

    private TeamRecruitmentApplication application(
        TeamRecruitment recruitment,
        User applicant,
        TeamRecruitmentApplicationStatus status
    ) {
        return TeamRecruitmentApplication.builder()
            .recruitment(recruitment)
            .applicant(applicant)
            .motivation("지원 동기")
            .availability("가능")
            .status(status)
            .profileSnapshot("{}")
            .build();
    }

    private TeamRecruitmentChatRoom chatRoom(
        TeamRecruitment recruitment,
        TeamRecruitmentApplication application,
        TeamRecruitmentChatRoomType roomType,
        String roomScopeKey
    ) {
        return TeamRecruitmentChatRoom.builder()
            .recruitment(recruitment)
            .roomScopeKey(roomScopeKey)
            .roomType(roomType)
            .application(application)
            .status(ACTIVE)
            .build();
    }

    private String rejectionEventKey(Integer applicationId) {
        return "team-recruitment:application:" + applicationId + ":APPLICATION_REJECTED";
    }

    private record Scenario(
        Integer failedRecruitmentId,
        Integer failedAcceptedId,
        Integer failedPendingId,
        Integer failedDirectRoomId,
        Integer successfulRecruitmentId,
        Integer successfulPendingId,
        Integer successfulApplicantId,
        Integer successfulTeamRoomId
    ) {
    }
}
