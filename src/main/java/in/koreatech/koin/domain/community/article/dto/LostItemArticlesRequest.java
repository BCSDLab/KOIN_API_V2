package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LostItemArticlesRequest(
    @Schema(description = "분실물 종류", example = "신분증", requiredMode = REQUIRED)
    String category,

    @Schema(description = "습득 장소", example = "학생회관 앞", requiredMode = REQUIRED)
    String location,

    @Schema(description = "습득 날짜", example = "2025-01-01", requiredMode = REQUIRED)
    LocalDate foundDate,

    @Schema(description = "본문", example = "학생회관 앞 계단에 …")
    String content,

    @Schema(description = "작성자", example = "총학생회", requiredMode = REQUIRED)
    String author,

    @Schema(description = "분실물 사진")
    List<String> images,

    @Schema(description = "등록일", example = "2025-01-10", requiredMode = REQUIRED)
    LocalDate registeredAt,

    @Schema(description = "수정일", example = "2025-01-10 16:53:22", requiredMode = REQUIRED)
    LocalDate updatedAt
    //List<InnerLostItemArticleRequest> requests
) {

    // private record InnerLostItemArticleRequest(
    //     @Schema(description = "분실물 종류", example = "신분증", requiredMode = REQUIRED)
    //     String category,
    //
    //     @Schema(description = "습득 장소", example = "학생회관 앞", requiredMode = REQUIRED)
    //     String location,
    //
    //     @Schema(description = "습득 날짜", example = "2025-01-01", requiredMode = REQUIRED)
    //     LocalDate foundDate,
    //
    //     @Schema(description = "본문", example = "학생회관 앞 계단에 …")
    //     String content,
    //
    //     @Schema(description = "작성자", example = "총학생회", requiredMode = REQUIRED)
    //     String author,
    //
    //     @Schema(description = "분실물 사진")
    //     List<String> images,
    //
    //     @Schema(description = "이전글 id", example = "17367")
    //     Integer prevId,
    //
    //     @Schema(description = "다음글 id", example = "17369")
    //     Integer nextId,
    //
    //     @Schema(description = "등록일", example = "2025-01-10", requiredMode = REQUIRED)
    //     LocalDate registeredAt,
    //
    //     @Schema(description = "수정일", example = "2025-01-10 16:53:22", requiredMode = REQUIRED)
    //     LocalDate updatedAt
    // ) {
    //
    // }
}
