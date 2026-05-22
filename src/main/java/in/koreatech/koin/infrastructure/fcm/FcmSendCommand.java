package in.koreatech.koin.infrastructure.fcm;

import in.koreatech.koin.common.model.MobileAppPath;

public record FcmSendCommand(
    String targetDeviceToken,
    String title,
    String content,
    String imageUrl,
    MobileAppPath path,
    String schemeUri,
    String type
) {
    public static FcmSendCommand of(
        String targetDeviceToken,
        String title,
        String content,
        String imageUrl,
        MobileAppPath path,
        String schemeUri,
        String type
    ) {
        return new FcmSendCommand(
            targetDeviceToken,
            title,
            content,
            imageUrl,
            path,
            schemeUri,
            type
        );
    }
}
