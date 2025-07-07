package in.koreatech.koin._common.event;

public record UserMarketingAgreementEvent(
    Integer userId,
    boolean marketingNotificationAgreement
) {

}
