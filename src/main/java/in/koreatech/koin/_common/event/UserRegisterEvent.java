package in.koreatech.koin._common.event;

public record UserRegisterEvent(
    Integer userId,
    boolean marketingNotificationAgreement
) {

}
