package in.koreatech.koin.unit.domain.team.recruitment.scheduler;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.MY_APPLICATIONS;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentOutboxLeaseService;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentOutboxProperties;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentOutboxWorker;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.infrastructure.fcm.FcmSendRequest;
import in.koreatech.koin.infrastructure.fcm.FcmSendResponse;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentOutboxWorkerTest {

    private static final Integer EVENT_ID = 1;
    private static final Integer RECIPIENT_ID = 2;
    private static final Integer RECRUITMENT_ID = 10;

    @Mock
    private TeamRecruitmentOutboxLeaseService leaseService;

    @Spy
    private TeamRecruitmentOutboxProperties properties = new TeamRecruitmentOutboxProperties();

    @Mock
    private TeamRecruitmentNotificationRepository notificationRepository;

    @Mock
    private TeamRecruitmentRepository recruitmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FcmClient fcmClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TeamRecruitmentOutboxWorker worker;

    @BeforeEach
    void enableWorker() {
        properties.setEnabled(true);
        properties.setBatchSize(10);
    }

    @Test
    void 기존_payload로_inbox_내용을_사용해_synchronous_FCM을_호출하고_완료한다() {
        User recipient = UserFixture.id_설정_코인_유저(RECIPIENT_ID);
        recipient.permitNotification("device-token");
        TeamRecruitment recruitment = recruitment();
        TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
            .recipient(recipient)
            .type(APPLICATION_REJECTED)
            .targetType(MY_APPLICATIONS)
            .messagePreview("모집 지원이 거절되었어요.")
            .recruitment(recruitment)
            .build();
        TeamRecruitmentOutboxLeaseService.OutboxClaim claim = claim(
            "{\"type\":\"APPLICATION_REJECTED\",\"target_type\":\"MY_APPLICATIONS\","
                + "\"recipient_id\":2,\"recruitment_id\":10,\"application_id\":20,\"chat_room_id\":null}"
        );
        when(leaseService.claim(any(), any(Integer.class))).thenReturn(List.of(claim));
        when(notificationRepository.findForOutbox(
            RECIPIENT_ID,
            APPLICATION_REJECTED,
            MY_APPLICATIONS,
            RECRUITMENT_ID,
            20,
            null
        )).thenReturn(List.of(notification));
        when(userRepository.findById(RECIPIENT_ID)).thenReturn(Optional.of(recipient));
        when(fcmClient.sendMessages(anyList())).thenReturn(List.of(FcmSendResponse.succeeded()));

        worker.processBatch();

        ArgumentCaptor<List<FcmSendRequest>> requestCaptor = ArgumentCaptor.forClass(List.class);
        verify(fcmClient).sendMessages(requestCaptor.capture());
        FcmSendRequest request = requestCaptor.getValue().get(0);
        assertThat(request.targetDeviceToken()).isEqualTo("device-token");
        assertThat(request.title()).isEqualTo("팀원 모집");
        assertThat(request.content()).isEqualTo("모집 지원이 거절되었어요.");
        assertThat(request.path()).isNull();
        assertThat(request.schemeUri()).isNull();
        assertThat(request.type()).isEqualTo("application_rejected");
        verify(leaseService).complete(eq(EVENT_ID), anyString());
    }

    @Test
    void notification_id가_있는_payload는_정확한_inbox를_조회한다() {
        User recipient = UserFixture.id_설정_코인_유저(RECIPIENT_ID);
        recipient.permitNotification("device-token");
        TeamRecruitment recruitment = recruitment();
        TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
            .id(90)
            .recipient(recipient)
            .type(APPLICATION_REJECTED)
            .targetType(MY_APPLICATIONS)
            .messagePreview("정확한 알림")
            .recruitment(recruitment)
            .application(TeamRecruitmentApplication.builder().id(20).recruitment(recruitment).build())
            .build();
        TeamRecruitmentOutboxLeaseService.OutboxClaim claim = claim(
            "{\"type\":\"APPLICATION_REJECTED\",\"target_type\":\"MY_APPLICATIONS\","
                + "\"recipient_id\":2,\"recruitment_id\":10,\"application_id\":20,"
                + "\"chat_room_id\":null,\"notification_id\":90}"
        );
        when(leaseService.claim(any(), any(Integer.class))).thenReturn(List.of(claim));
        when(notificationRepository.findByIdForOutbox(90)).thenReturn(Optional.of(notification));
        when(userRepository.findById(RECIPIENT_ID)).thenReturn(Optional.of(recipient));
        when(fcmClient.sendMessages(anyList())).thenReturn(List.of(FcmSendResponse.succeeded()));

        worker.processBatch();

        verify(notificationRepository).findByIdForOutbox(90);
        verify(notificationRepository, never()).findForOutbox(
            any(), any(), any(), any(), any(), any()
        );
        verify(fcmClient).sendMessages(anyList());
        verify(leaseService).complete(eq(EVENT_ID), anyString());
    }

    @Test
    void 삭제된_inbox_이벤트는_FCM없이_완료한다() {
        TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
            .id(90)
            .recipient(UserFixture.id_설정_코인_유저(RECIPIENT_ID))
            .type(APPLICATION_REJECTED)
            .targetType(MY_APPLICATIONS)
            .messagePreview("삭제된 알림")
            .recruitment(recruitment())
            .application(TeamRecruitmentApplication.builder().id(20).recruitment(recruitment()).build())
            .isDeleted(true)
            .build();
        TeamRecruitmentOutboxLeaseService.OutboxClaim claim = claim(
            "{\"type\":\"APPLICATION_REJECTED\",\"target_type\":\"MY_APPLICATIONS\","
                + "\"recipient_id\":2,\"recruitment_id\":10,\"application_id\":20,"
                + "\"chat_room_id\":null,\"notification_id\":90}"
        );
        when(leaseService.claim(any(), any(Integer.class))).thenReturn(List.of(claim));
        when(notificationRepository.findByIdForOutbox(90)).thenReturn(Optional.of(notification));

        worker.processBatch();

        verify(fcmClient, never()).sendMessages(anyList());
        verify(userRepository, never()).findById(any());
        verify(leaseService).complete(eq(EVENT_ID), anyString());
        verify(leaseService, never()).fail(any(), any(), any(), anyBoolean());
    }

    @Test
    void notification_id가_있는_payload와_inbox_식별자가_다르면_전송하지_않고_실패한다() {
        TeamRecruitment recruitment = recruitment();
        TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
            .id(90)
            .recipient(UserFixture.id_설정_코인_유저(3))
            .type(APPLICATION_REJECTED)
            .targetType(MY_APPLICATIONS)
            .messagePreview("다른 수신자")
            .recruitment(recruitment)
            .application(TeamRecruitmentApplication.builder().id(20).recruitment(recruitment).build())
            .build();
        TeamRecruitmentOutboxLeaseService.OutboxClaim claim = claim(
            "{\"type\":\"APPLICATION_REJECTED\",\"target_type\":\"MY_APPLICATIONS\","
                + "\"recipient_id\":2,\"recruitment_id\":10,\"application_id\":20,"
                + "\"chat_room_id\":null,\"notification_id\":90}"
        );
        when(leaseService.claim(any(), any(Integer.class))).thenReturn(List.of(claim));
        when(notificationRepository.findByIdForOutbox(90)).thenReturn(Optional.of(notification));

        worker.processBatch();

        verify(fcmClient, never()).sendMessages(anyList());
        verify(leaseService).fail(
            eq(EVENT_ID),
            anyString(),
            eq("NOTIFICATION_TARGET_MISMATCH"),
            eq(false)
        );
        verify(userRepository, never()).findById(any());
    }

    @Test
    void notification_id가_없는_삭제된_legacy_inbox도_전송하지_않고_완료한다() {
        TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
            .recipient(UserFixture.id_설정_코인_유저(RECIPIENT_ID))
            .type(APPLICATION_REJECTED)
            .targetType(MY_APPLICATIONS)
            .messagePreview("삭제된 legacy 알림")
            .recruitment(recruitment())
            .isDeleted(true)
            .build();
        TeamRecruitmentOutboxLeaseService.OutboxClaim claim = claim(
            "{\"type\":\"APPLICATION_REJECTED\",\"target_type\":\"MY_APPLICATIONS\","
                + "\"recipient_id\":2,\"recruitment_id\":10,\"application_id\":20,\"chat_room_id\":null}"
        );
        when(leaseService.claim(any(), any(Integer.class))).thenReturn(List.of(claim));
        when(notificationRepository.findForOutbox(
            RECIPIENT_ID,
            APPLICATION_REJECTED,
            MY_APPLICATIONS,
            RECRUITMENT_ID,
            20,
            null
        )).thenReturn(List.of(notification));

        worker.processBatch();

        verify(fcmClient, never()).sendMessages(anyList());
        verify(leaseService).complete(eq(EVENT_ID), anyString());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void notification_id가_없는_legacy_payload가_복수_inbox와_일치하면_전송하지_않고_실패한다() {
        TeamRecruitmentNotification first = TeamRecruitmentNotification.builder()
            .recipient(UserFixture.id_설정_코인_유저(RECIPIENT_ID))
            .type(APPLICATION_REJECTED)
            .targetType(MY_APPLICATIONS)
            .messagePreview("첫 번째")
            .recruitment(recruitment())
            .build();
        TeamRecruitmentNotification second = TeamRecruitmentNotification.builder()
            .recipient(UserFixture.id_설정_코인_유저(RECIPIENT_ID))
            .type(APPLICATION_REJECTED)
            .targetType(MY_APPLICATIONS)
            .messagePreview("두 번째")
            .recruitment(recruitment())
            .build();
        TeamRecruitmentOutboxLeaseService.OutboxClaim claim = claim(
            "{\"type\":\"APPLICATION_REJECTED\",\"target_type\":\"MY_APPLICATIONS\","
                + "\"recipient_id\":2,\"recruitment_id\":10,\"application_id\":20,\"chat_room_id\":null}"
        );
        when(leaseService.claim(any(), any(Integer.class))).thenReturn(List.of(claim));
        when(notificationRepository.findForOutbox(
            RECIPIENT_ID,
            APPLICATION_REJECTED,
            MY_APPLICATIONS,
            RECRUITMENT_ID,
            20,
            null
        )).thenReturn(List.of(first, second));

        worker.processBatch();

        verify(fcmClient, never()).sendMessages(anyList());
        verify(leaseService).fail(
            eq(EVENT_ID),
            anyString(),
            eq("AMBIGUOUS_NOTIFICATION_TARGET"),
            eq(false)
        );
        verify(recruitmentRepository, never()).findById(any());
    }

    @Test
    void notification_id가_있는_payload의_inbox가_없으면_legacy로_대체하지_않는다() {
        TeamRecruitmentOutboxLeaseService.OutboxClaim claim = claim(
            "{\"type\":\"APPLICATION_REJECTED\",\"target_type\":\"MY_APPLICATIONS\","
                + "\"recipient_id\":2,\"recruitment_id\":10,\"application_id\":20,"
                + "\"chat_room_id\":null,\"notification_id\":90}"
        );
        when(leaseService.claim(any(), any(Integer.class))).thenReturn(List.of(claim));
        when(notificationRepository.findByIdForOutbox(90)).thenReturn(Optional.empty());

        worker.processBatch();

        verify(notificationRepository, never()).findForOutbox(
            any(), any(), any(), any(), any(), any()
        );
        verify(fcmClient, never()).sendMessages(anyList());
        verify(leaseService).fail(
            eq(EVENT_ID),
            anyString(),
            eq("NOTIFICATION_NOT_FOUND"),
            eq(false)
        );
    }

    @Test
    void device_token이_없는_사용자는_FCM을_호출하지_않고_terminal_failed로_처리한다() {
        User recipient = UserFixture.id_설정_코인_유저(RECIPIENT_ID);
        when(leaseService.claim(any(), any(Integer.class))).thenReturn(List.of(claim(
            "{\"type\":\"APPLICATION_REJECTED\",\"target_type\":\"MY_APPLICATIONS\","
                + "\"recipient_id\":2,\"recruitment_id\":10,\"application_id\":20,\"chat_room_id\":null}"
        )));
        when(notificationRepository.findForOutbox(
            RECIPIENT_ID,
            APPLICATION_REJECTED,
            MY_APPLICATIONS,
            RECRUITMENT_ID,
            20,
            null
        )).thenReturn(List.of());
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment()));
        when(userRepository.findById(RECIPIENT_ID)).thenReturn(Optional.of(recipient));

        worker.processBatch();

        verify(fcmClient, never()).sendMessages(anyList());
        verify(leaseService).fail(any(), any(), org.mockito.ArgumentMatchers.eq("NO_DEVICE_TOKEN"), org.mockito.ArgumentMatchers.eq(false));
    }

    @Test
    void FCM_UNREGISTERED는_재시도하지_않는다() {
        User recipient = UserFixture.id_설정_코인_유저(RECIPIENT_ID);
        recipient.permitNotification("device-token");
        when(leaseService.claim(any(), any(Integer.class))).thenReturn(List.of(claim(
            "{\"type\":\"APPLICATION_REJECTED\",\"target_type\":\"MY_APPLICATIONS\","
                + "\"recipient_id\":2,\"recruitment_id\":10,\"application_id\":20,\"chat_room_id\":null}"
        )));
        when(notificationRepository.findForOutbox(
            RECIPIENT_ID,
            APPLICATION_REJECTED,
            MY_APPLICATIONS,
            RECRUITMENT_ID,
            20,
            null
        )).thenReturn(List.of());
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment()));
        when(userRepository.findById(RECIPIENT_ID)).thenReturn(Optional.of(recipient));
        when(fcmClient.sendMessages(anyList()))
            .thenReturn(List.of(FcmSendResponse.failed("INVALID_ARGUMENT", "UNREGISTERED")));

        worker.processBatch();

        verify(leaseService).fail(any(), any(), org.mockito.ArgumentMatchers.eq("INVALID_ARGUMENT"), org.mockito.ArgumentMatchers.eq(false));
    }

    private TeamRecruitmentOutboxLeaseService.OutboxClaim claim(String payload) {
        return new TeamRecruitmentOutboxLeaseService.OutboxClaim(EVENT_ID, "event-key", payload);
    }

    private TeamRecruitment recruitment() {
        return TeamRecruitment.builder()
            .id(RECRUITMENT_ID)
            .author(UserFixture.id_설정_코인_유저(1))
            .title("팀원 모집")
            .description("모집 내용")
            .build();
    }
}
