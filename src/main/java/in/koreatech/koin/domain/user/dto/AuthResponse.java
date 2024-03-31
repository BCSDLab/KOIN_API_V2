package in.koreatech.koin.domain.user.dto;

public record AuthResponse(
    boolean isSuccess,
    String errorMessage
) {

}
