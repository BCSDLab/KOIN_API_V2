package in.koreatech.koin.domain.community.keyword.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record KeywordFilterRequest(
    @NotBlank(message = "키워드는 공백일 수 없습니다.")
    @Schema(example = "키워드", description = "필터링 할 키워드 단어", requiredMode = REQUIRED)
    String keyword,

    @Schema(example = "true", description = "필터링 여부", requiredMode = REQUIRED)
    Boolean isFiltered
) {
}
