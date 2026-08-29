package in.koreatech.koin.infrastructure.fcm;

import in.koreatech.koin.common.model.MobileAppPath;

public record FcmSendRequest(
    String targetDeviceToken,
    String title,
    String content,
    String imageUrl,
    MobileAppPath path,
    String schemeUri,
    String type,
    String notificationId
) {
    public FcmSendRequest(
        String targetDeviceToken,
        String title,
        String content,
        String imageUrl,
        MobileAppPath path,
        String schemeUri,
        String type
    ) {
        this(targetDeviceToken, title, content, imageUrl, path, schemeUri, type, null);
    }

    public static FcmSendRequest of(
        String targetDeviceToken,
        String title,
        String content,
        String imageUrl,
        MobileAppPath path,
        String schemeUri,
        String type
    ) {
        return new FcmSendRequest(
            targetDeviceToken,
            title,
            content,
            imageUrl,
            path,
            schemeUri,
            type,
            null
        );
    }

    public static FcmSendRequest of(
        String targetDeviceToken,
        String title,
        String content,
        String imageUrl,
        MobileAppPath path,
        String schemeUri,
        String type,
        String notificationId
    ) {
        return new FcmSendRequest(
            targetDeviceToken,
            title,
            content,
            imageUrl,
            path,
            schemeUri,
            type,
            notificationId
        );
    }
}
