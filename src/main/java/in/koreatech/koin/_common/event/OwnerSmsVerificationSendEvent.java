package in.koreatech.koin._common.event;

public record OwnerSmsVerificationSendEvent(
    String verificationCode,
    String phoneNumber
) {

}
