package in.koreatech.koin.global.domain.notification.service;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.global.domain.notification.dto.NotificationSubscribePermitRequest;
import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
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
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

    public void push(Notification notification) {
        notificationRepository.save(notification);
        String deviceToken = notification.getUser().getDeviceToken();
        fcmClient.sendMessage(
            deviceToken,
            notification.getTitle(),
            notification.getMessage(),
            notification.getImageUrl(),
            notification.getMobileAppPath(),
            notification.getType()
        );
    }

    public void pushSoldOutNotification() {
        Notification notification = notificationFactory.generateSoldOutNotification();
        notificationRepository.save(notification);
        List<String> deviceTokenList = generateDeviceTokenListBySubscribeSoldOut();
        fcmClient.sendMessageAll(
            deviceTokenList,
            notification.getTitle(),
            notification.getMessage(),
            notification.getImageUrl(),
            notification.getUrl(),
            notification.getType()
        );
    }

    public List<String> generateDeviceTokenListBySubscribeSoldOut() {
        List<NotificationSubscribe> noti = notificationSubscribeRepository.findAllBySubscribeType(DINING_SOLD_OUT);
        List<String> deviceTokenList = new ArrayList<>();
        for (NotificationSubscribe n : noti) {
            User user = userRepository.getById(n.getUser().getId());
            if (!user.getDeviceToken().isEmpty()) {
                deviceTokenList.add(user.getDeviceToken());
            }
        }
        return deviceTokenList;
    }

    public NotificationStatusResponse checkNotification(Long userId) {
        User user = userRepository.getById(userId);
        boolean isPermit = user.getDeviceToken() != null;
        List<NotificationSubscribe> notificationSubscribes = notificationSubscribeRepository.findAllByUserId(userId);
        return NotificationStatusResponse.of(isPermit, notificationSubscribes);
    }

    @Transactional
    public void permitNotification(Long userId, String deviceToken) {
        User user = userRepository.getById(userId);
        user.permitNotification(deviceToken);
    }

    @Transactional
    public void permitNotificationSubscribe(Long userId, NotificationSubscribePermitRequest request) {
        if (notificationSubscribeRepository.findByUserIdAndSubscribeType(userId, request.type()).isPresent()) {
            return;
        }
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
