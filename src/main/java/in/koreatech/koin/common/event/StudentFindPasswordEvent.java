package in.koreatech.koin.common.event;

public record StudentFindPasswordEvent(
    String email,
    String serverUrl,
    String resetToken
) {
}
