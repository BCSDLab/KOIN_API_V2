package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AuthResponse(
    @Schema(description = "사용자 권한 타입", example = "STUDENT")
    String userType
) {

    public static AuthResponse from(User user) {
        return new AuthResponse(user.getUserType().getValue());
    }
}
