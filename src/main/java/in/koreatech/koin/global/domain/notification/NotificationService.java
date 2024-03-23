package in.koreatech.koin.global.domain.notification;

import org.springframework.stereotype.Service;

import in.koreatech.koin.global.fcm.FcmClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FcmClient fcmClient;

    public void push(Notification notification) {
        notificationRepository.save(notification);
        String deviceToken = notification.getUser().getDeviceToken();
        fcmClient.sendMessage(
            deviceToken,
            notification.getTitle(),
            notification.getMessage(),
            notification.getImageUrl(),
            notification.getUrl(),
            notification.getType()
        );
    }
}
