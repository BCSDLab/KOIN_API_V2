package in.koreatech.koin.unit.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.NotificationPersistenceService;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPersistenceService notificationPersistenceService;

    @Mock
    private FcmClient fcmClient;

    @Mock
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Mock
    private NotificationFactory notificationFactory;

    @Test
    @DisplayName("알림 전송 결과 조회는 전송 성공 시에만 알림 레코드를 저장한다.")
    void pushNotificationsWithResult_whenDelivered_savesNotification() {
        Notification notification = createNotification("device-token");
        when(fcmClient.sendMessageWithResult(anyString(), anyString(), anyString(), any(), any(), anyString(), anyString()))
            .thenReturn(true);

        List<NotificationService.NotificationDeliveryResult> result = notificationService.pushNotificationsWithResult(
            List.of(notification)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).delivered()).isTrue();
        InOrder inOrder = inOrder(fcmClient, notificationPersistenceService);
        inOrder.verify(fcmClient).sendMessageWithResult(
            anyString(), anyString(), anyString(), any(), any(), anyString(), anyString()
        );
        inOrder.verify(notificationPersistenceService).saveAfterSend(notification);
    }

    @Test
    @DisplayName("알림 전송 실패 시 알림 레코드를 저장하지 않는다.")
    void pushNotificationsWithResult_whenDeliveryFails_doesNotSaveNotification() {
        Notification notification = createNotification("device-token");
        when(fcmClient.sendMessageWithResult(anyString(), anyString(), anyString(), any(), any(), anyString(), anyString()))
            .thenReturn(false);

        List<NotificationService.NotificationDeliveryResult> result = notificationService.pushNotificationsWithResult(
            List.of(notification)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).delivered()).isFalse();
        verify(notificationPersistenceService, never()).saveAfterSend(notification);
    }

    @Test
    @DisplayName("배치 알림 중 일부 저장이 실패해도 발송 결과는 유지하고 다음 알림을 계속 처리한다.")
    void pushNotificationsWithResult_whenSaveFails_keepsDeliveryResultAndContinuesNextNotification() {
        Notification firstNotification = createNotification("device-token-1");
        Notification secondNotification = createNotification("device-token-2");
        when(fcmClient.sendMessageWithResult(anyString(), anyString(), anyString(), any(), any(), anyString(), anyString()))
            .thenReturn(true, true);
        doThrow(new RuntimeException("save fail")).when(notificationPersistenceService).saveAfterSend(firstNotification);

        List<NotificationService.NotificationDeliveryResult> result = notificationService.pushNotificationsWithResult(
            List.of(firstNotification, secondNotification)
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).delivered()).isTrue();
        assertThat(result.get(1).delivered()).isTrue();
        verify(notificationPersistenceService).saveAfterSend(firstNotification);
        verify(notificationPersistenceService).saveAfterSend(secondNotification);
    }

    @Test
    @DisplayName("배치 알림은 전송 성공 여부를 각각 반환하고 성공한 알림만 저장한다.")
    void pushNotificationsWithResult_whenBatchContainsMixedResults_returnsEachResult() {
        Notification firstNotification = createNotification("device-token-1");
        Notification secondNotification = createNotification("device-token-2");
        when(fcmClient.sendMessageWithResult(anyString(), anyString(), anyString(), any(), any(), anyString(), anyString()))
            .thenReturn(true, false);

        List<NotificationService.NotificationDeliveryResult> result = notificationService.pushNotificationsWithResult(
            List.of(firstNotification, secondNotification)
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).delivered()).isTrue();
        assertThat(result.get(1).delivered()).isFalse();
        verify(notificationPersistenceService).saveAfterSend(firstNotification);
        verify(notificationPersistenceService, never()).saveAfterSend(secondNotification);
    }

    @Test
    @DisplayName("단건 알림 전송은 저장 후 FCM 전송을 수행한다.")
    void pushNotification_savesNotificationBeforeSend() {
        Notification notification = createNotification("device-token");

        notificationService.pushNotification(notification);

        InOrder inOrder = inOrder(notificationRepository, fcmClient);
        inOrder.verify(notificationRepository).saveAll(List.of(notification));
        inOrder.verify(fcmClient).sendMessage(
            anyString(), anyString(), anyString(), any(), any(), anyString(), anyString()
        );
        verify(notificationRepository, never()).save(notification);
    }

    private Notification createNotification(String deviceToken) {
        User user = UserFixture.id_설정_코인_유저(1);
        user.permitNotification(deviceToken);

        Notification notification = mock(Notification.class);
        when(notification.getUser()).thenReturn(user);
        when(notification.getTitle()).thenReturn("title");
        when(notification.getMessage()).thenReturn("message");
        when(notification.getImageUrl()).thenReturn(null);
        when(notification.getMobileAppPath()).thenReturn(null);
        when(notification.getSchemeUri()).thenReturn("scheme-uri");
        when(notification.getType()).thenReturn("message");
        return notification;
    }
}
