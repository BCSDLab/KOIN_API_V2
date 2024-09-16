package in.koreatech.koin.admin.version.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record AdminVersionRequest(
    @Schema(description = "종류 - 최대 20자", example = "원룸")
    @Size(max = 20, message = "방종류의 최대 길이는 20자입니다.")
    String type
) {
}
