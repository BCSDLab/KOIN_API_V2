package in.koreatech.koin.unit.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.repository.NotificationJdbcRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
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
    private FcmClient fcmClient;

    @Mock
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Mock
    private NotificationJdbcRepository notificationJdbcRepository;

    @Test
    @DisplayName("단건 알림 전송은 저장 후 FCM 전송을 수행한다.")
    void pushNotification_savesNotificationBeforeSend() {
        Notification notification = createNotification("device-token");

        notificationService.pushNotification(notification);

        InOrder inOrder = inOrder(notificationJdbcRepository, fcmClient);
        inOrder.verify(notificationJdbcRepository).batchInsert(List.of(notification));
        inOrder.verify(fcmClient).sendMessage(
            anyString(), anyString(), anyString(), any(), any(), anyString(), anyString()
        );
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
