package in.koreatech.koin._common.event;

public record StudentRegisterRequestEvent(
    String email,
    String serverUrl,
    String authToken
) {

}
