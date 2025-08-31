package in.koreatech.koin.common.event;

public record UserEmailVerificationSendEvent(
    String verificationCode,
    String email
) {

}
