package in.koreatech.koin._common.event;

public record UserEmailVerificationSendEvent(
    String verificationCode,
    String email
) {

}
