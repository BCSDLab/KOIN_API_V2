package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FindIdResponse(
    @Schema(description = "사용자의 ID", example = "user3452", requiredMode = REQUIRED)
    String userId
) {

    public static FindIdResponse from(String userId) {
        return new FindIdResponse(userId);
    }
}
