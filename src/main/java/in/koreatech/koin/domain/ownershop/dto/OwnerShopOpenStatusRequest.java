package in.koreatech.koin.domain.ownershop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnerShopOpenStatusRequest(
    @Schema(description = "영업 여부. true면 영업 시작, false면 영업 종료", example = "true", requiredMode = REQUIRED)
    @NotNull(message = "영업 여부는 필수입니다.")
    Boolean isOpen
) {

}
