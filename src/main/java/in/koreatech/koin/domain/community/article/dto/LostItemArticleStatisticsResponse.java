package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record LostItemArticleStatisticsResponse(

    @Schema(description = "찾음 상태의 분실물 게시글 개수", example = "13", requiredMode = REQUIRED)
    Integer foundCount,

    @Schema(description = "찾는 중 상태의 분실물 게시글 개수", example = "36", requiredMode = REQUIRED)
    Integer notFoundCount
) {
}
