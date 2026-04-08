package in.koreatech.koin.domain.callvan.event;

import java.util.List;

public record CallvanPushNotificationEvent(
    List<Integer> notificationIds
) {

}
