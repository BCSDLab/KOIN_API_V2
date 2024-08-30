package in.koreatech.koin.global.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.model.AccessHistory;
import in.koreatech.koin.domain.user.model.Device;
import in.koreatech.koin.domain.user.repository.AccessHistoryRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.global.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.global.domain.notification.exception.NotificationNotPermitException;
import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.global.fcm.FcmClient;
import in.koreatech.koin.global.useragent.UserAgentInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmClient fcmClient;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final AccessHistoryRepository accessHistoryRepository;

    public void push(List<Notification> notifications) {
        for (Notification notification : notifications) {
            push(notification);
        }
    }

    public void push(Notification notification) {
        notificationRepository.save(notification);
        String deviceToken = notification.getDevice().getFcmToken();
        fcmClient.sendMessage(
            deviceToken,
            notification.getTitle(),
            notification.getMessage(),
            notification.getImageUrl(),
            notification.getMobileAppPath(),
            notification.getSchemeUri(),
            notification.getType()
        );
    }

    public NotificationStatusResponse checkNotification(Integer userId, String ipAddress) {
        userRepository.getById(userId);
        Device device = accessHistoryRepository.getByPublicIp(ipAddress).getDevice();
        boolean isPermit = device.getFcmToken() != null;
        List<NotificationSubscribe> subscribeList = notificationSubscribeRepository.findAllByDeviceId(device.getId());
        return NotificationStatusResponse.of(isPermit, subscribeList);
    }

    @Transactional
    public void permitNotification(Integer userId, UserAgentInfo userAgentInfo, String ipAddress, String deviceToken) {
        AccessHistory accessHistory = userService.findOrCreateAccessHistory(ipAddress);
        Device device = userService.createDeviceIfNotExists(userId, userAgentInfo, accessHistory);
        device.permitNotification(deviceToken);
    }

    @Transactional
    public void permitNotificationSubscribe(Integer userId, String ipAddress, NotificationSubscribeType subscribeType) {
        userRepository.getById(userId);
        Device device = accessHistoryRepository.getByPublicIp(ipAddress).getDevice();
        validateFcmToken(device);
        if (notificationSubscribeRepository
            .findByDeviceIdAndSubscribeTypeAndDetailType(device.getId(), subscribeType, null).isPresent()) {
            return;
        }
        NotificationSubscribe notificationSubscribe = NotificationSubscribe.builder()
            .device(device)
            .subscribeType(subscribeType)
            .build();
        notificationSubscribeRepository.save(notificationSubscribe);
    }

    @Transactional
    public void permitNotificationDetailSubscribe(Integer userId,
        String ipAddress, NotificationDetailSubscribeType detailType) {
        userRepository.getById(userId);
        Device device = accessHistoryRepository.getByPublicIp(ipAddress).getDevice();
        NotificationSubscribeType type = NotificationSubscribeType.getParentType(detailType);
        validateFcmToken(device);
        if (notificationSubscribeRepository
            .findByDeviceIdAndSubscribeTypeAndDetailType(device.getId(), type, null).isEmpty()) {
            throw NotificationNotPermitException.withDetail("deviceId: " + device.getId() + ", type: " + type);
        }
        if (notificationSubscribeRepository
            .findByDeviceIdAndSubscribeTypeAndDetailType(device.getId(), type, detailType).isPresent()) {
            return;
        }

        NotificationSubscribe detailSubscribe = NotificationSubscribe.builder()
            .device(device)
            .subscribeType(type)
            .detailType(detailType)
            .build();
        notificationSubscribeRepository.save(detailSubscribe);
    }

    @Transactional
    public void rejectNotification(Integer userId, String ipAddress) {
        userRepository.getById(userId);
        Device device = accessHistoryRepository.getByPublicIp(ipAddress).getDevice();
        device.rejectNotification();
    }

    @Transactional
    public void rejectNotificationByType(Integer userId, String ipAddress, NotificationSubscribeType subscribeType) {
        userRepository.getById(userId);
        Device device = accessHistoryRepository.getByPublicIp(ipAddress).getDevice();
        validateFcmToken(device);
        if (notificationSubscribeRepository
            .findByDeviceIdAndSubscribeTypeAndDetailType(device.getId(), subscribeType, null).isEmpty()) {
            return;
        }
        notificationSubscribeRepository.deleteByDeviceIdAndSubscribeTypeAndDetailType(device.getId(), subscribeType, null);
    }

    @Transactional
    public void rejectNotificationDetailSubscribe(Integer userId,
        String ipAddress, NotificationDetailSubscribeType detailType) {
        userRepository.getById(userId);
        NotificationSubscribeType type = NotificationSubscribeType.getParentType(detailType);
        Device device = accessHistoryRepository.getByPublicIp(ipAddress).getDevice();
        validateFcmToken(device);
        if (notificationSubscribeRepository
            .findByDeviceIdAndSubscribeTypeAndDetailType(device.getId(), type, null).isEmpty()) {
            throw NotificationNotPermitException.withDetail("deviceId: " + device.getId() + ", type: " + type);
        }
        if (notificationSubscribeRepository
            .findByDeviceIdAndSubscribeTypeAndDetailType(device.getId(), type, detailType).isEmpty()) {
            return;
        }
        notificationSubscribeRepository.deleteByDeviceIdAndSubscribeTypeAndDetailType(device.getId(), type, detailType);
    }

    private static void validateFcmToken(Device device) {
        if (device.getFcmToken() == null) {
            throw NotificationNotPermitException.withDetail("user.deviceToken: null");
        }
    }
}
