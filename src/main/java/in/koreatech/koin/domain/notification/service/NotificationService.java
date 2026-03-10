package in.koreatech.koin.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.DINING;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.getParentType;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import in.koreatech.koin.domain.dining.model.DiningType;
import in.koreatech.koin.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.domain.notification.exception.NotificationNotPermitException;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    public record NotificationDeliveryResult(Notification notification, boolean delivered) {}

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmClient fcmClient;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationFactory notificationFactory;

    @Transactional
    public void pushNotifications(List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return;
        }
        notificationRepository.saveAll(notifications);
        runAfterCommit(() -> notifications.forEach(this::sendNotificationSafely));
    }

    @Transactional
    public List<NotificationDeliveryResult> pushNotificationsWithResult(List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return List.of();
        }

        List<NotificationDeliveryResult> deliveryResults = new ArrayList<>(notifications.size());
        // afterCommit 콜백은 트랜잭션 프록시가 반환되기 전에 실행되므로 호출자는 채워진 결과를 받는다.
        runAfterCommit(() -> notifications.forEach(notification ->
            deliveryResults.add(pushNotificationWithResult(notification))
        ));
        return deliveryResults;
    }

    @Transactional
    public void pushNotification(Notification notification) {
        pushNotifications(List.of(notification));
    }

    private NotificationDeliveryResult pushNotificationWithResult(Notification notification) {
        try {
            boolean delivered = sendNotificationWithResult(notification);
            if (!delivered) {
                return new NotificationDeliveryResult(notification, false);
            }
            notificationRepository.save(notification);
            return new NotificationDeliveryResult(notification, delivered);
        } catch (Exception e) {
            log.warn("알림 전송 처리 중 예외가 발생했습니다.", e);
            return new NotificationDeliveryResult(notification, false);
        }
    }

    public NotificationStatusResponse getNotificationInfo(Integer userId) {
        User user = userRepository.getById(userId);
        boolean isPermit = StringUtils.isNotBlank(user.getDeviceToken());
        List<NotificationSubscribe> subscribeList = notificationSubscribeRepository.findAllByUserId(userId);
        return NotificationStatusResponse.of(isPermit, subscribeList);
    }

    @Transactional
    public void permitNotification(Integer userId, String deviceToken) {
        User user = userRepository.getById(userId);
        user.permitNotification(deviceToken);
    }

    @Transactional
    public void permitNotificationSubscribe(Integer userId, NotificationSubscribeType subscribeType) {
        User user = userRepository.getById(userId);
        ensureUserDeviceToken(user.getDeviceToken());
        if (notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(userId, subscribeType)) {
            return;
        }
        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .user(user)
            .subscribeType(subscribeType)
            .build();
        notificationSubscribeRepository.save(notificationSubscribe);
    }

    @Transactional
    public void permitNotificationDetailSubscribe(Integer userId, NotificationDetailSubscribeType detailType) {
        User user = userRepository.getById(userId);
        ensureUserDeviceToken(user.getDeviceToken());

        NotificationSubscribeType type = getParentType(detailType);
        if (notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailType(userId, type, detailType)) {
            return;
        }

        NotificationSubscribe detailSubscribe = NotificationSubscribe.builder()
            .user(user)
            .subscribeType(type)
            .detailType(detailType)
            .build();
        notificationSubscribeRepository.save(detailSubscribe);
    }

    @Transactional
    public void rejectNotification(Integer userId) {
        User user = userRepository.getById(userId);
        user.rejectNotification();
    }

    @Transactional
    public void rejectNotificationBySubscribeType(Integer userId, NotificationSubscribeType subscribeType) {
        User user = userRepository.getById(userId);
        ensureUserDeviceToken(user.getDeviceToken());
        notificationSubscribeRepository.deleteByUserIdAndSubscribeTypeAndDetailTypeIsNull(userId, subscribeType);
    }

    @Transactional
    public void rejectNotificationByDetailType(Integer userId, NotificationDetailSubscribeType detailType) {
        User user = userRepository.getById(userId);
        ensureUserDeviceToken(user.getDeviceToken());
        notificationSubscribeRepository.deleteByUserIdAndDetailType(userId, detailType);
    }

    @Transactional
    public void sendDiningSoldOutNotifications(Integer dinningId, String place, DiningType diningType) {
        NotificationDetailSubscribeType detailType = NotificationDetailSubscribeType.from(diningType);
        var notifications = notificationSubscribeRepository.findAllBySubscribeTypeAndDetailType(DINING_SOLD_OUT, detailType)
            .stream()
            .map(subscribe -> notificationFactory.generateSoldOutNotification(
                DINING,
                dinningId,
                place,
                subscribe.getUser()
            ))
            .toList();
        pushNotifications(notifications);
    }

    private void sendNotificationSafely(Notification notification) {
        try {
            sendNotification(notification);
        } catch (Exception e) {
            log.warn("알림 전송 처리 중 예외가 발생했습니다.", e);
        }
    }

    private void sendNotification(Notification notification) {
        String deviceToken = notification.getUser().getDeviceToken();
        fcmClient.sendMessage(
            deviceToken,
            notification.getTitle(),
            notification.getMessage(),
            notification.getImageUrl(),
            notification.getMobileAppPath(),
            notification.getSchemeUri(),
            notification.getType().toLowerCase()
        );
    }

    private boolean sendNotificationWithResult(Notification notification) {
        String deviceToken = notification.getUser().getDeviceToken();
        return fcmClient.sendMessageWithResult(
            deviceToken,
            notification.getTitle(),
            notification.getMessage(),
            notification.getImageUrl(),
            notification.getMobileAppPath(),
            notification.getSchemeUri(),
            notification.getType().toLowerCase()
        );
    }

    private void runAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
            || !TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }

        // Rollback된 데이터에 대한 푸시 전송을 막기 위해 커밋 이후에만 FCM을 호출한다.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }

    private void ensureUserDeviceToken(String deviceToken) {
        if (StringUtils.isBlank(deviceToken)) {
            throw NotificationNotPermitException.withDetail("user.deviceToken: null");
        }
    }
}
