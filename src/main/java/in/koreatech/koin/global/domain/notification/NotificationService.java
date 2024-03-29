package in.koreatech.koin.global.domain.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.dto.NotificationStatusResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.notification.dto.NotificationSubscribePermitRequest;
import in.koreatech.koin.global.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.global.fcm.FcmClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmClient fcmClient;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

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

    public NotificationStatusResponse checkNotification(Long userId) {
        User user = userRepository.getById(userId);
        return new NotificationStatusResponse(user.getDeviceToken() != null);
    }

    @Transactional
    public void permitNotification(Long userId, String deviceToken) {
        User user = userRepository.getById(userId);
        user.permitNotification(deviceToken);
    }

    @Transactional
    public void permitNotificationSubscribe(Long userId, NotificationSubscribePermitRequest request) {
        User user = userRepository.getById(userId);
        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .user(user)
            .subscribeType(request.type())
            .build();
        notificationSubscribeRepository.save(notificationSubscribe);
    }

    @Transactional
    public void rejectNotification(Long userId) {
        User user = userRepository.getById(userId);
        user.rejectNotification();
    }

    @Transactional
    public void rejectNotificationByType(Long userId, NotificationSubscribeType subscribeType) {
        notificationSubscribeRepository.deleteByUserIdAndSubscribeType(userId, subscribeType);
    }
}
