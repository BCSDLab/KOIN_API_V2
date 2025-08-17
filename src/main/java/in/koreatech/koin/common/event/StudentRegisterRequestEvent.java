package in.koreatech.koin.common.event;

public record StudentRegisterRequestEvent(
    String email,
    String serverUrl,
    String authToken
) {

}
