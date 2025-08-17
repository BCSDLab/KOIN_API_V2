package in.koreatech.koin.common.event;

public record UserMarketingAgreementEvent(
    Integer userId,
    boolean marketingNotificationAgreement
) {

}
