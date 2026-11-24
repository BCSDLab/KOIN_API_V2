package in.koreatech.koin.domain.chat.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(SnakeCaseStrategy.class)
public record ChatReadRequest(
    @NotBlank
    @Pattern(regexp = "[1-9][0-9]{0,18}")
    String messageId
) {

    public Long parsedMessageId() {
        try {
            long parsed = Long.parseLong(messageId);
            if (parsed < 1) {
                throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
    }
}
