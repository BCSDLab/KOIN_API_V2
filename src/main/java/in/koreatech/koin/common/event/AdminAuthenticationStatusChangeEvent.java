package in.koreatech.koin.common.event;

public record AdminAuthenticationStatusChangeEvent(
    String changedByAdminId,
    String changedByAdminName,
    String targetAdminId,
    String targetAdminName,
    Boolean isAuthed
) {

}
