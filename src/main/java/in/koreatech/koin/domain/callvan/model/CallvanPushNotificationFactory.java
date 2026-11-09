package in.koreatech.koin.domain.callvan.model;

import static in.koreatech.koin.common.model.MobileAppPath.CALLVAN;
import static in.koreatech.koin.common.model.MobileAppPath.CALLVAN_CHAT;
import static in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType.DEPARTURE_UPCOMING;
import static in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType.NEW_MESSAGE;
import static in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType.PARTICIPANT_JOINED;
import static in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType.RECRUITMENT_COMPLETE;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.notification.i18n.NotificationMessages;
import in.koreatech.koin.common.model.MobileAppPath;

@Component
public class CallvanPushNotificationFactory {

    public CallvanPushNotification from(CallvanNotification notification) {
        MobileAppPath path = notification.getNotificationType() == NEW_MESSAGE ? CALLVAN_CHAT : CALLVAN;
        String schemeUri = generateSchemeUri(notification);
        String title = generateTitle(notification);
        String message = localizedPreview(notification);

        return new CallvanPushNotification(
            path,
            schemeUri,
            title,
            message,
            null,
            notification.getNotificationType().name().toLowerCase(),
            notification.getRecipient()
        );
    }

    private String generateTitle(CallvanNotification notification) {
        return switch (notification.getNotificationType()) {
            case NEW_MESSAGE -> NotificationMessages.format(notification.getRecipient(), "콜벤팟 %s님의 메시지", notification.getSenderNickname());
            case PARTICIPANT_JOINED -> NotificationMessages.format(notification.getRecipient(), "콜벤팟 새 참여자");
            case RECRUITMENT_COMPLETE -> NotificationMessages.format(notification.getRecipient(), "콜벤팟 인원 모집 완료");
            case DEPARTURE_UPCOMING -> NotificationMessages.format(notification.getRecipient(), "콜벤팟 출발 30분 전");
            case REPORT_WARNING, REPORT_RESTRICTION_14_DAYS, REPORT_PERMANENT_RESTRICTION -> NotificationMessages.format(notification.getRecipient(), "콜벤팟 이용 안내");
        };
    }

    public static String localizedPreview(CallvanNotification notification) {
        return switch (notification.getNotificationType()) {
            case PARTICIPANT_JOINED -> NotificationMessages.format(notification.getRecipient(), "%s님이 콜벤팟에 참여했어요", notification.getJoinedMemberNickname());
            case DEPARTURE_UPCOMING -> NotificationMessages.format(notification.getRecipient(), "%s -> %s 콜벤팟이 30분 뒤 출발해요",
                getLocationName(notification.getDepartureCustomName(), notification.getDepartureType().getName()),
                getLocationName(notification.getArrivalCustomName(), notification.getArrivalType().getName())
            );
            case NEW_MESSAGE, RECRUITMENT_COMPLETE, REPORT_WARNING, REPORT_RESTRICTION_14_DAYS,
                 REPORT_PERMANENT_RESTRICTION -> notification.getNotificationType() == NEW_MESSAGE
                    ? notification.getMessagePreview()
                    : NotificationMessages.translateKnown(notification.getRecipient(), notification.getMessagePreview());
        };
    }

    private String generateSchemeUri(CallvanNotification notification) {
        Integer postId = notification.getPost() != null ? notification.getPost().getId() : null;
        if (notification.getNotificationType() == NEW_MESSAGE) {
            return "callvan-chat?postId=%d&chatRoomId=%d".formatted(postId, notification.getChatRoom().getId());
        }
        return "callvan?id=%d".formatted(postId);
    }

    private static String getLocationName(String customName, String defaultName) {
        if (StringUtils.hasText(customName)) {
            return customName;
        }
        return defaultName;
    }
}
