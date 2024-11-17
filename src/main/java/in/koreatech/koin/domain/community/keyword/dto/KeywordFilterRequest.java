package in.koreatech.koin.domain.community.keyword.dto;

import jakarta.validation.constraints.NotBlank;

public record KeywordFilterRequest(
    @NotBlank(message = "키워드는 공백일 수 없습니다.") String keyword,
    Boolean isFiltered
) {
}
