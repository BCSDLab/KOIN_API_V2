package in.koreatech.koin._common.event;

public record StudentFindPasswordEvent(
    String email,
    String serverUrl,
    String resetToken
) {
}
