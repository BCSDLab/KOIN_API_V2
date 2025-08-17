package in.koreatech.koin.common.event;

public record UserSmsVerificationSendEvent(
    String verificationCode,
    String phoneNumber
) {

}
