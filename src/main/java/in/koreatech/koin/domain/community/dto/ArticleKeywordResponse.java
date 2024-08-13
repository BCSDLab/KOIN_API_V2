package in.koreatech.koin.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ArticleKeywordResponse(
    @Schema(description = "id", example = "1")
    Integer id,

    @Schema(description = "keyword", example = "수강신청")
    String keyword
) {

}
