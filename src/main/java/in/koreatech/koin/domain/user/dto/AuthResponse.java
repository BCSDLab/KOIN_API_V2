package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AuthResponse(
    @Schema(description = "사용자 권한 타입", example = "STUDENT", requiredMode = REQUIRED)
    String userType
) {

    public static AuthResponse from(User user) {
        return new AuthResponse(user.getUserType().getValue());
    }
}
