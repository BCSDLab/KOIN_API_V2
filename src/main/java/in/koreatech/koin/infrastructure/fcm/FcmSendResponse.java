package in.koreatech.koin.infrastructure.fcm;

public record FcmSendResponse(
    boolean success,
    String errorCode,
    String messagingErrorCode
) {
    public static FcmSendResponse succeeded() {
        return new FcmSendResponse(true, null, null);
    }

    public static FcmSendResponse failed(String errorCode, String messagingErrorCode) {
        return new FcmSendResponse(false, errorCode, messagingErrorCode);
    }
}
