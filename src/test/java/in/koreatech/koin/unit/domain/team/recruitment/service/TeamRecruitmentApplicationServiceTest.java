package in.koreatech.koin.unit.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.NEW_APPLICATION;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.RECRUITMENT_CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.ROLE_BASED;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_DUPLICATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_FINALIZED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CAPACITY_FULL;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CLOSED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_PROFILE_REQUIRED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ROLE_CLOSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicationCreatedResponse;
import in.koreatech.koin.domain.team.recruitment.dto.CreateApplicationRequest;
import in.koreatech.koin.domain.team.recruitment.dto.UpdateApplicationStatusRequest;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileActivity;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileSkill;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileActivityRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileSkillRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRoleRepository;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentApplicationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.StudentFixture;
import in.koreatech.koin.unit.fixture.UserFixture;
import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.hibernate.exception.ConstraintViolationException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentApplicationServiceTest {

    private static final Integer AUTHOR_ID = 1;
    private static final Integer APPLICANT_ID = 2;
    private static final Integer RECRUITMENT_ID = 10;
    private static final Integer APPLICATION_ID = 20;
    private static final Integer ROLE_ID = 30;
    private static final Integer TEAM_ROOM_ID = 40;
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 28);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private TeamRecruitmentRepository recruitmentRepository;

    @Mock
    private TeamRecruitmentRoleRepository roleRepository;

    @Mock
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Mock
    private TeamRecruitmentProfileRepository profileRepository;

    @Mock
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Mock
    private TeamRecruitmentChatMemberRepository chatMemberRepository;

    @Mock
    private TeamRecruitmentChatMessageRepository chatMessageRepository;

    @Mock
    private TeamRecruitmentNotificationRepository notificationRepository;

    @Mock
    private TeamRecruitmentOutboxEventRepository outboxEventRepository;

    @Mock
    private TeamRecruitmentProfileActivityRepository profileActivityRepository;

    @Mock
    private TeamRecruitmentProfileSkillRepository profileSkillRepository;

    @Mock
    private StudentRepository studentRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private EntityManager entityManager;

    @Mock
    private Clock clock;

    @InjectMocks
    private TeamRecruitmentApplicationService applicationService;

    @BeforeEach
    void setUpClock() {
        Instant now = Instant.parse("2026-08-28T03:00:00Z");
        lenient().when(clock.instant()).thenReturn(now);
        lenient().when(clock.getZone()).thenReturn(KST);
        lenient().when(clock.withZone(KST)).thenReturn(clock);
    }

    @Nested
    class CreateApplication {

        @Test
        void 지원을_생성하고_지원_당시_프로필_snapshot을_저장한다() throws Exception {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.plusDays(1));
            TeamRecruitmentRole role = role(recruitment, 0, 2);
            stubRecruitment(recruitment);
            stubApplicantAndProfile();
            stubNoExistingApplication();
            when(roleRepository.findByIdAndRecruitmentIdWithLock(ROLE_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(role));

            CreateApplicationRequest request = new CreateApplicationRequest(
                ROLE_ID,
                "지원 동기",
                "월수금 20시 이후"
            );

            ApplicationCreatedResponse response = applicationService.createApplication(
                request,
                RECRUITMENT_ID,
                APPLICANT_ID
            );

            ArgumentCaptor<TeamRecruitmentApplication> captor =
                ArgumentCaptor.forClass(TeamRecruitmentApplication.class);
            verify(applicationRepository).save(captor.capture());
            TeamRecruitmentApplication saved = captor.getValue();
            JsonNode snapshot = objectMapper.readTree(saved.getProfileSnapshot());

            assertThat(response.status()).isEqualTo(PENDING);
            assertThat(response.recruitmentId()).isEqualTo(RECRUITMENT_ID);
            assertThat(response.role().id()).isEqualTo(ROLE_ID);
            assertThat(saved.getApplicant().getId()).isEqualTo(APPLICANT_ID);
            assertThat(saved.getRole()).isSameAs(role);
            assertThat(saved.getMotivation()).isEqualTo("지원 동기");
            assertThat(saved.getAvailability()).isEqualTo("월수금 20시 이후");
            assertThat(saved.getStatus()).isEqualTo(PENDING);
            assertThat(snapshot.get("nickname").asText()).isEqualTo("지원자");
            assertThat(snapshot.get("student_year").asInt()).isEqualTo(2020);
            assertThat(snapshot.get("preferred_role").asText()).isEqualTo("백엔드");
            assertThat(snapshot.get("skills").get(0).asText()).isEqualTo("Spring");
            assertThat(snapshot.get("activities").get(0).get("title").asText()).isEqualTo("KOIN 프로젝트");
            assertThat(snapshot.get("self_introduction").asText()).isEqualTo("소개");
        }

        @Test
        void 지원_생성시_작성자에게_NEW_APPLICATION_inbox와_outbox를_저장한다() throws Exception {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.plusDays(1));
            TeamRecruitmentRole role = role(recruitment, 0, 2);
            stubRecruitment(recruitment);
            stubApplicantAndProfile();
            stubNoExistingApplication();
            when(roleRepository.findByIdAndRecruitmentIdWithLock(ROLE_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(role));
            when(outboxEventRepository.findByEventKey(anyString())).thenReturn(Optional.empty());
            when(notificationRepository.save(any())).thenAnswer(invocation -> {
                TeamRecruitmentNotification notification = invocation.getArgument(0);
                ReflectionTestUtils.setField(notification, "id", 50);
                return notification;
            });

            applicationService.createApplication(
                new CreateApplicationRequest(ROLE_ID, "지원 동기", "월수금 20시 이후"),
                RECRUITMENT_ID,
                APPLICANT_ID
            );

            ArgumentCaptor<TeamRecruitmentNotification> notificationCaptor =
                ArgumentCaptor.forClass(TeamRecruitmentNotification.class);
            verify(notificationRepository).save(notificationCaptor.capture());
            TeamRecruitmentNotification notification = notificationCaptor.getValue();
            assertThat(notification.getRecipient()).isSameAs(recruitment.getAuthor());
            assertThat(notification.getType()).isEqualTo(NEW_APPLICATION);
            assertThat(notification.getTargetType().name()).isEqualTo("APPLICANT_MANAGEMENT");
            assertThat(notification.getMessagePreview())
                .isEqualTo("팀원 모집에 새로운 지원자가 있어요.");

            ArgumentCaptor<TeamRecruitmentOutboxEvent> outboxCaptor =
                ArgumentCaptor.forClass(TeamRecruitmentOutboxEvent.class);
            verify(outboxEventRepository).save(outboxCaptor.capture());
            JsonNode payload = objectMapper.readTree(outboxCaptor.getValue().getPayload());
            assertThat(payload.get("type").asText()).isEqualTo(NEW_APPLICATION.name());
            assertThat(payload.get("target_type").asText()).isEqualTo("APPLICANT_MANAGEMENT");
            assertThat(payload.get("recipient_id").asInt()).isEqualTo(AUTHOR_ID);
            assertThat(payload.get("recruitment_id").asInt()).isEqualTo(RECRUITMENT_ID);
            assertThat(payload.has("application_id")).isTrue();
            assertThat(payload.get("notification_id").asInt()).isEqualTo(50);
            assertThat(payload.get("chat_room_id").isNull()).isTrue();
        }

        @Test
        void 작성자는_자기_모집글에_지원할_수_없다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.plusDays(1));
            stubRecruitment(recruitment);

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(ROLE_ID, "지원 동기", "가능"),
                RECRUITMENT_ID,
                AUTHOR_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_FORBIDDEN));
            verify(applicationRepository, never()).save(any());
        }

        @Test
        void 팀원_모집_프로필이_없으면_지원을_생성할_수_없다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.plusDays(1));
            TeamRecruitmentRole role = role(recruitment, 0, 2);
            stubRecruitment(recruitment);
            stubNoExistingApplication();
            when(studentRepository.getById(APPLICANT_ID)).thenReturn(applicant());
            when(profileRepository.findByUser_Id(APPLICANT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(ROLE_ID, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_PROFILE_REQUIRED));
        }

        @Test
        void 지원자의_학과가_없으면_지원을_생성할_수_없다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 5, TODAY.plusDays(1));
            Student student = StudentFixture.익명_학생(APPLICANT_ID, null);
            ReflectionTestUtils.setField(student.getUser(), "id", APPLICANT_ID);
            stubRecruitment(recruitment);
            stubNoExistingApplication();
            stubApplicantAndProfile(student);

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(null, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_PROFILE_REQUIRED));
            verify(applicationRepository, never()).save(any());
        }

        @Test
        void 지원자의_학번에서_입학_연도를_확인할_수_없으면_지원을_생성할_수_없다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 5, TODAY.plusDays(1));
            Student student = applicant();
            student.updateStudentNumber("학번없음");
            stubRecruitment(recruitment);
            stubNoExistingApplication();
            stubApplicantAndProfile(student);

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(null, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_PROFILE_REQUIRED));
            verify(applicationRepository, never()).save(any());
        }

        @Test
        void 지원자_중복_제약을_위반하면_지원_중복_오류로_변환한다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 5, TODAY.plusDays(1));
            stubRecruitment(recruitment);
            stubApplicantAndProfile();
            stubNoExistingApplication();
            DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "insert failed",
                new ConstraintViolationException(
                    "duplicate application",
                    new SQLException("duplicate key"),
                    "uk_team_recruitment_application_recruitment_applicant"
                )
            );
            when(applicationRepository.save(any())).thenThrow(exception);

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(null, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, thrown ->
                    assertThat(thrown.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_APPLICATION_DUPLICATE));
        }

        @Test
        void 다른_무결성_제약은_중복_오류로_숨기지_않고_원래_예외를_전파한다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 5, TODAY.plusDays(1));
            stubRecruitment(recruitment);
            stubApplicantAndProfile();
            stubNoExistingApplication();
            DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "insert failed",
                new ConstraintViolationException(
                    "foreign key violation",
                    new SQLException("foreign key"),
                    "fk_team_recruitment_application_applicant"
                )
            );
            when(applicationRepository.save(any())).thenThrow(exception);

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(null, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            )).isSameAs(exception);
        }

        @Test
        void GENERAL_모집은_role_id를_null로_강제한다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 5, TODAY.plusDays(1));
            stubRecruitment(recruitment);
            stubApplicantAndProfile();
            stubNoExistingApplication();

            ApplicationCreatedResponse response = applicationService.createApplication(
                new CreateApplicationRequest(null, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            );

            ArgumentCaptor<TeamRecruitmentApplication> captor =
                ArgumentCaptor.forClass(TeamRecruitmentApplication.class);
            verify(applicationRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isNull();
            assertThat(response.role()).isNull();
        }

        @Test
        void GENERAL_모집에_role_id를_보내면_잘못된_요청이다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 5, TODAY.plusDays(1));
            stubRecruitment(recruitment);
            stubApplicantAndProfile();
            stubNoExistingApplication();

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(ROLE_ID, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(INVALID_REQUEST_BODY));
        }

        @Test
        void ROLE_BASED_모집은_role_id가_필수다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.plusDays(1));
            stubRecruitment(recruitment);
            stubApplicantAndProfile();
            stubNoExistingApplication();

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(null, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(INVALID_REQUEST_BODY));
        }

        @Test
        void ROLE_BASED_모집의_마감된_role에는_지원할_수_없다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.plusDays(1));
            TeamRecruitmentRole role = role(recruitment, 2, 2);
            stubRecruitment(recruitment);
            stubApplicantAndProfile();
            stubNoExistingApplication();
            when(roleRepository.findByIdAndRecruitmentIdWithLock(ROLE_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(role));

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(ROLE_ID, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_ROLE_CLOSED));
        }

        @Test
        void 이미_지원한_모집글에는_중복_지원할_수_없다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.plusDays(1));
            TeamRecruitmentApplication existing = application(recruitment, null, ACCEPTED);
            stubRecruitment(recruitment);
            when(applicationRepository.findByRecruitmentIdAndApplicantIdWithLock(RECRUITMENT_ID, APPLICANT_ID))
                .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(ROLE_ID, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_APPLICATION_DUPLICATE));
            verify(studentRepository, never()).getById(any());
        }

        @Test
        void 모집글이_이미_마감되면_지원을_받지_않는다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.plusDays(1));
            ReflectionTestUtils.setField(recruitment, "status", CLOSED);
            stubRecruitment(recruitment);

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(ROLE_ID, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_CLOSED));
        }

        @Test
        void 지원_마감일이_지나면_지원을_받지_않는다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.minusDays(1));
            stubRecruitment(recruitment);

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(ROLE_ID, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_CLOSED));
        }

        @Test
        void 전체_모집_정원이_가득_차면_지원을_받지_않는다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 5, 5, TODAY.plusDays(1));
            TeamRecruitmentRole role = role(recruitment, 0, 5);
            stubRecruitment(recruitment);
            stubApplicantAndProfile();
            stubNoExistingApplication();
            when(roleRepository.findByIdAndRecruitmentIdWithLock(ROLE_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(role));

            assertThatThrownBy(() -> applicationService.createApplication(
                new CreateApplicationRequest(ROLE_ID, "지원 동기", "가능"),
                RECRUITMENT_ID,
                APPLICANT_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_CAPACITY_FULL));
        }
    }

    @Nested
    class UpdateApplicationStatus {

        @Test
        void PENDING_지원서를_REJECTED로_전이한다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 5, TODAY.plusDays(1));
            TeamRecruitmentApplication application = application(recruitment, null, PENDING);
            stubDecision(recruitment, application);
            when(outboxEventRepository.findByEventKey(anyString())).thenReturn(Optional.empty());

            applicationService.updateApplicationStatus(
                new UpdateApplicationStatusRequest(REJECTED),
                RECRUITMENT_ID,
                APPLICATION_ID,
                AUTHOR_ID
            );

            assertThat(application.getStatus()).isEqualTo(REJECTED);
            assertThat(application.getDecisionReason()).isEqualTo("APPLICATION_REJECTED");
            verify(applicationRepository).save(application);
            verify(notificationRepository).save(any());
            verify(outboxEventRepository).save(any());
        }

        @Test
        void ACCEPTED_전이시_전체와_역할_승인_수를_증가시키고_정원에_도달하면_모집을_닫는다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 1, TODAY.plusDays(1));
            TeamRecruitmentRole role = role(recruitment, 0, 1);
            TeamRecruitmentApplication application = application(recruitment, role, PENDING);
            TeamRecruitmentChatRoom teamRoom = TeamRecruitmentChatRoom.builder()
                .id(TEAM_ROOM_ID)
                .recruitment(recruitment)
                .roomScopeKey("TEAM")
                .roomType(TEAM)
                .status(ACTIVE)
                .build();
            stubDecision(recruitment, application);
            when(roleRepository.findByIdAndRecruitmentIdWithLock(ROLE_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(role));
            when(chatRoomRepository.findByRecruitmentIdAndRoomScopeKeyWithLock(RECRUITMENT_ID, "TEAM"))
                .thenReturn(Optional.of(teamRoom));
            when(chatMemberRepository.existsByChatRoom_IdAndUser_Id(TEAM_ROOM_ID, APPLICANT_ID))
                .thenReturn(false);
            when(chatMessageRepository.findTopByChatRoom_IdOrderByIdDesc(TEAM_ROOM_ID))
                .thenReturn(Optional.of(TeamRecruitmentChatMessage.builder()
                    .id(99)
                    .chatRoom(teamRoom)
                    .sender(recruitment.getAuthor())
                    .senderNickname("작성자")
                    .content("기존 메시지")
                    .isImage(false)
                    .build()));
            when(outboxEventRepository.findByEventKey(anyString())).thenReturn(Optional.empty());
            when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
                eq(RECRUITMENT_ID),
                eq(List.of(PENDING)),
                any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of()));
            when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
                eq(RECRUITMENT_ID),
                eq(List.of(ACCEPTED)),
                any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of()));

            applicationService.updateApplicationStatus(
                new UpdateApplicationStatusRequest(ACCEPTED),
                RECRUITMENT_ID,
                APPLICATION_ID,
                AUTHOR_ID
            );

            assertThat(application.getStatus()).isEqualTo(ACCEPTED);
            assertThat(recruitment.getCurrentParticipants()).isEqualTo(1);
            assertThat(role.getCurrentParticipants()).isEqualTo(1);
            assertThat(role.isClosed()).isTrue();
            assertThat(recruitment.getStatus()).isEqualTo(CLOSED);
            ArgumentCaptor<TeamRecruitmentChatMember> memberCaptor = ArgumentCaptor.forClass(
                TeamRecruitmentChatMember.class
            );
            verify(chatMemberRepository).save(memberCaptor.capture());
            assertThat(memberCaptor.getValue().getLastReadMessageId()).isEqualTo(99);
            verify(applicationRepository).save(application);
            verify(roleRepository).save(role);
            verify(chatRoomRepository).save(teamRoom);
            verify(notificationRepository).save(any());
            verify(outboxEventRepository).save(any());
        }

        @Test
        void 이미_처리된_지원서는_다시_처리할_수_없다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 5, TODAY.plusDays(1));
            TeamRecruitmentApplication application = application(recruitment, null, ACCEPTED);
            stubDecision(recruitment, application);

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                new UpdateApplicationStatusRequest(REJECTED),
                RECRUITMENT_ID,
                APPLICATION_ID,
                AUTHOR_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_APPLICATION_FINALIZED));
            verify(applicationRepository, never()).save(any());
        }

        @Test
        void PENDING은_상태_변경_요청으로_허용하지_않는다() {
            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                new UpdateApplicationStatusRequest(PENDING),
                RECRUITMENT_ID,
                APPLICATION_ID,
                AUTHOR_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(INVALID_REQUEST_BODY));
            verify(recruitmentRepository, never()).findByIdWithLock(any());
            verify(applicationRepository, never()).save(any());
        }

        @Test
        void 전체_정원이_가득_차면_지원서를_승인할_수_없다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 5, 5, TODAY.plusDays(1));
            TeamRecruitmentApplication application = application(recruitment, null, PENDING);
            stubDecision(recruitment, application);

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                new UpdateApplicationStatusRequest(ACCEPTED),
                RECRUITMENT_ID,
                APPLICATION_ID,
                AUTHOR_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_CAPACITY_FULL));
            verify(applicationRepository, never()).save(any());
        }

        @Test
        void 역할_정원이_가득_차면_지원서를_승인할_수_없다() {
            TeamRecruitment recruitment = recruitment(ROLE_BASED, 0, 5, TODAY.plusDays(1));
            TeamRecruitmentRole role = role(recruitment, 1, 1);
            TeamRecruitmentApplication application = application(recruitment, role, PENDING);
            stubDecision(recruitment, application);
            when(roleRepository.findByIdAndRecruitmentIdWithLock(ROLE_ID, RECRUITMENT_ID))
                .thenReturn(Optional.of(role));

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                new UpdateApplicationStatusRequest(ACCEPTED),
                RECRUITMENT_ID,
                APPLICATION_ID,
                AUTHOR_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_ROLE_CLOSED));
            verify(applicationRepository, never()).save(any());
        }

        @Test
        void 마지막_자리를_승인하면_남은_PENDING을_거절하고_두번째_승인으로_count를_초과하지_않는다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 1, TODAY.plusDays(1));
            TeamRecruitmentApplication target = application(recruitment, null, PENDING);
            TeamRecruitmentApplication remaining = application(21, recruitment, null, PENDING);
            TeamRecruitmentChatRoom teamRoom = TeamRecruitmentChatRoom.builder()
                .id(TEAM_ROOM_ID)
                .recruitment(recruitment)
                .roomScopeKey("TEAM")
                .roomType(TEAM)
                .status(ACTIVE)
                .build();
            stubDecision(recruitment, target);
            when(chatRoomRepository.findByRecruitmentIdAndRoomScopeKeyWithLock(RECRUITMENT_ID, "TEAM"))
                .thenReturn(Optional.of(teamRoom));
            when(chatMemberRepository.existsByChatRoom_IdAndUser_Id(TEAM_ROOM_ID, APPLICANT_ID))
                .thenReturn(false);
            when(outboxEventRepository.findByEventKey(anyString())).thenReturn(Optional.empty());
            when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
                eq(RECRUITMENT_ID),
                eq(List.of(PENDING)),
                any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of(remaining)));
            when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
                eq(RECRUITMENT_ID),
                eq(List.of(ACCEPTED)),
                any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of(target)));

            applicationService.updateApplicationStatus(
                new UpdateApplicationStatusRequest(ACCEPTED),
                RECRUITMENT_ID,
                APPLICATION_ID,
                AUTHOR_ID
            );

            assertThat(target.getStatus()).isEqualTo(ACCEPTED);
            assertThat(remaining.getStatus()).isEqualTo(REJECTED);
            assertThat(remaining.getDecisionReason()).isEqualTo("RECRUITMENT_CLOSED");
            assertThat(recruitment.getCurrentParticipants()).isEqualTo(1);
            assertThat(recruitment.getStatus()).isEqualTo(CLOSED);
            verify(applicationRepository, org.mockito.Mockito.times(2)).save(any());

            ArgumentCaptor<TeamRecruitmentNotification> notificationCaptor =
                ArgumentCaptor.forClass(TeamRecruitmentNotification.class);
            verify(notificationRepository, org.mockito.Mockito.times(3)).save(notificationCaptor.capture());
            assertThat(notificationCaptor.getAllValues())
                .extracting(TeamRecruitmentNotification::getType)
                .containsExactlyInAnyOrder(APPLICATION_ACCEPTED, RECRUITMENT_CLOSED, APPLICATION_REJECTED);
            verify(outboxEventRepository, org.mockito.Mockito.times(3)).save(any());

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                new UpdateApplicationStatusRequest(ACCEPTED),
                RECRUITMENT_ID,
                21,
                AUTHOR_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_CLOSED));
            assertThat(recruitment.getCurrentParticipants()).isEqualTo(1);
            assertThat(remaining.getStatus()).isEqualTo(REJECTED);
        }

        @Test
        void 모집이_마감되면_승인과_거절을_처리하지_않는다() {
            TeamRecruitment recruitment = recruitment(GENERAL, 0, 5, TODAY.plusDays(1));
            ReflectionTestUtils.setField(recruitment, "status", CLOSED);
            stubRecruitment(recruitment);

            assertThatThrownBy(() -> applicationService.updateApplicationStatus(
                new UpdateApplicationStatusRequest(ACCEPTED),
                RECRUITMENT_ID,
                APPLICATION_ID,
                AUTHOR_ID
            ))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                    assertThat(exception.getErrorCode()).isEqualTo(TEAM_RECRUITMENT_CLOSED));
            verify(applicationRepository, never()).save(any());
        }
    }

    private void stubRecruitment(TeamRecruitment recruitment) {
        when(recruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
    }

    private void stubNoExistingApplication() {
        when(applicationRepository.findByRecruitmentIdAndApplicantIdWithLock(RECRUITMENT_ID, APPLICANT_ID))
            .thenReturn(Optional.empty());
        when(applicationRepository.findByRecruitment_IdAndApplicant_Id(RECRUITMENT_ID, APPLICANT_ID))
            .thenReturn(Optional.empty());
    }

    private void stubApplicantAndProfile() {
        stubApplicantAndProfile(applicant());
    }

    private void stubApplicantAndProfile(Student student) {
        TeamRecruitmentProfile profile = TeamRecruitmentProfile.builder()
            .userId(APPLICANT_ID)
            .user(student.getUser())
            .profileNickname("지원자")
            .preferredRole("백엔드")
            .selfIntroduction("소개")
            .build();
        TeamRecruitmentProfileSkill skill = TeamRecruitmentProfileSkill.builder()
            .id(51)
            .profile(profile)
            .skill("Spring")
            .displayOrder(1)
            .build();
        TeamRecruitmentProfileActivity activity = TeamRecruitmentProfileActivity.builder()
            .id(52)
            .profile(profile)
            .title("KOIN 프로젝트")
            .startedAt(LocalDate.of(2025, 3, 1))
            .endedAt(LocalDate.of(2025, 6, 30))
            .isOngoing(false)
            .description("팀 프로젝트 활동")
            .displayOrder(1)
            .build();
        when(studentRepository.getById(APPLICANT_ID)).thenReturn(student);
        when(profileRepository.findByUser_Id(APPLICANT_ID)).thenReturn(Optional.of(profile));
        when(profileSkillRepository.findAllByProfile_UserIdOrderByDisplayOrderAsc(APPLICANT_ID))
            .thenReturn(List.of(skill));
        when(profileActivityRepository.findAllByProfile_UserIdOrderByDisplayOrderAsc(APPLICANT_ID))
            .thenReturn(List.of(activity));
    }

    private Student applicant() {
        Student student = StudentFixture.익명_학생(
            APPLICANT_ID,
            Department.builder().name("컴퓨터공학부").build()
        );
        ReflectionTestUtils.setField(student.getUser(), "id", APPLICANT_ID);
        return student;
    }

    private void stubDecision(
        TeamRecruitment recruitment,
        TeamRecruitmentApplication application
    ) {
        stubRecruitment(recruitment);
        when(applicationRepository.findByIdAndRecruitmentIdWithLock(APPLICATION_ID, RECRUITMENT_ID))
            .thenReturn(Optional.of(application));
    }

    private TeamRecruitment recruitment(
        in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType type,
        Integer currentParticipants,
        Integer maxParticipants,
        LocalDate deadlineDate
    ) {
        return TeamRecruitment.builder()
            .id(RECRUITMENT_ID)
            .author(UserFixture.id_설정_코인_유저(AUTHOR_ID))
            .category(in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT)
            .title("팀원 모집")
            .meetingType(in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE)
            .activityStartDate(TODAY.plusDays(2))
            .activityEndDate(TODAY.plusDays(10))
            .deadlineDate(deadlineDate)
            .recruitmentType(type)
            .maxParticipants(maxParticipants)
            .currentParticipants(currentParticipants)
            .description("모집 내용")
            .status(RECRUITING)
            .build();
    }

    private TeamRecruitmentRole role(
        TeamRecruitment recruitment,
        Integer currentParticipants,
        Integer maxParticipants
    ) {
        TeamRecruitmentRole role = TeamRecruitmentRole.builder()
            .id(ROLE_ID)
            .recruitment(recruitment)
            .name("백엔드")
            .maxParticipants(maxParticipants)
            .currentParticipants(currentParticipants)
            .displayOrder(1)
            .build();
        recruitment.addRole(role);
        return role;
    }

    private TeamRecruitmentApplication application(
        TeamRecruitment recruitment,
        TeamRecruitmentRole role,
        TeamRecruitmentApplicationStatus status
    ) {
        return application(APPLICATION_ID, recruitment, role, status);
    }

    private TeamRecruitmentApplication application(
        Integer applicationId,
        TeamRecruitment recruitment,
        TeamRecruitmentRole role,
        TeamRecruitmentApplicationStatus status
    ) {
        User applicant = UserFixture.id_설정_코인_유저(APPLICANT_ID);
        return TeamRecruitmentApplication.builder()
            .id(applicationId)
            .recruitment(recruitment)
            .applicant(applicant)
            .role(role)
            .motivation("지원 동기")
            .availability("가능")
            .status(status)
            .profileSnapshot("{}")
            .build();
    }
}
