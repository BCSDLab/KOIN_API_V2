package in.koreatech.koin.common.event;

public record OwnerSmsVerificationSendEvent(
    String verificationCode,
    String phoneNumber
) {

}
