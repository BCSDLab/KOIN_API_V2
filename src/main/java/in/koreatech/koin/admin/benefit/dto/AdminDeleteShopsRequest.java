package in.koreatech.koin.admin.benefit.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.validation.NotBlankElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminDeleteShopsRequest(
    @Schema(description = "상점 ID 리스트", example = "[1, 2, 5]", requiredMode = REQUIRED)
    @NotNull(message = "상점 ID 리스트는 필수입니다.")
    @NotBlankElement(message = "상점 ID 리스트는 빈 요소가 존재할 수 없습니다.")
    List<Integer> shopIds
) {

}
