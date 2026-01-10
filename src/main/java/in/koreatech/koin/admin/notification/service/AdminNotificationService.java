package in.koreatech.koin.admin.notification.service;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_DETAIL_SUBSCRIBE_TYPE;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.admin.manager.repository.AdminRepository;
import in.koreatech.koin.admin.notification.dto.AdminNotificationRequest;
import in.koreatech.koin.admin.notification.repository.AdminNotificationRepository;
import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;
    private final AdminRepository adminRepository;

    @Transactional
    public void sendNotification(AdminNotificationRequest request, Integer adminId) {

    }

    private void validateDetailTypeMatchesSubscribeType(
        NotificationSubscribeType subscribeType, NotificationDetailSubscribeType detailSubscribeType
    ) {
        if (detailSubscribeType == null) {
            return;
        }

        if (subscribeType.isNotContainsDetailType(detailSubscribeType)) {
            throw CustomException.of(
                INVALID_DETAIL_SUBSCRIBE_TYPE,
                String.format("subscribeType: %s, detailSubscribeType: %s", subscribeType, detailSubscribeType)
            );
        }
    }
}
