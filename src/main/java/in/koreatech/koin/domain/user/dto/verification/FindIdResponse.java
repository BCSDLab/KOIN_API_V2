package in.koreatech.koin.domain.user.dto.verification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record FindIdResponse(
    @Schema(description = "로그인 ID", example = "user3452", requiredMode = REQUIRED)
    String userId
) {

    public static FindIdResponse from(String userId) {
        return new FindIdResponse(userId);
    }
}
