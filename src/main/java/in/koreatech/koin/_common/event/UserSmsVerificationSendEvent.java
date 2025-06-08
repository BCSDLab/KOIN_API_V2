package in.koreatech.koin._common.event;

public record UserSmsVerificationSendEvent(
    String verificationCode,
    String phoneNumber
) {

}
