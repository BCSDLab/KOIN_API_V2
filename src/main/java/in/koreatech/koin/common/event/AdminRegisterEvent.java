package in.koreatech.koin.common.event;

public record AdminRegisterEvent(
    String creatorId,
    String creatorName,
    String newAdminId,
    String newAdminName
) {

}
