package in.koreatech.koin.domain.callvan.model;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.user.model.User;

public record CallvanPushNotification(
    MobileAppPath mobileAppPath,
    String schemeUri,
    String title,
    String message,
    String imageUrl,
    String type,
    User recipient
) {

}
