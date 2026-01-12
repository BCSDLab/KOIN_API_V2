package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.model.LostItemImage;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record LostItemArticleResponse(
    @Schema(description = "게시글 id", example = "17368", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "게시판 id", example = "14", requiredMode = REQUIRED)
    Integer boardId,

    @Schema(description = "게시글 타입", example = "LOST", requiredMode = NOT_REQUIRED)
    String type,

    @Schema(description = "분실물 종류", example = "신분증", requiredMode = REQUIRED)
    String category,

    @Schema(description = "습득 장소", example = "학생회관 앞", requiredMode = REQUIRED)
    String foundPlace,

    @Schema(description = "습득 날짜", example = "2025-01-01", requiredMode = REQUIRED)
    LocalDate foundDate,

    @Schema(description = "본문", example = "학생회관 앞 계단에 …")
    String content,

    @Schema(description = "작성자", example = "총학생회", requiredMode = REQUIRED)
    String author,

    @Schema(description = "총학생회 여부", example = "1", requiredMode = NOT_REQUIRED)
    Boolean isCouncil,

    @Schema(description = "내 게시글 여부", example = "1", requiredMode = NOT_REQUIRED)
    Boolean isMine,

    @Schema(description = "분실물 게시글 찾음 상태 여부", example = "false", requiredMode = REQUIRED)
    Boolean isFound,

    @Schema(description = "분실물 사진")
    List<InnerLostItemImageResponse> images,

    @Schema(description = "이전글 id", example = "17367")
    Integer prevId,

    @Schema(description = "다음글 id", example = "17369")
    Integer nextId,

    @Schema(description = "등록일", example = "2025-01-10", requiredMode = REQUIRED)
    LocalDate registeredAt,

    @Schema(description = "수정일", example = "2025-01-10 16:53:22", requiredMode = REQUIRED)
    LocalDateTime updatedAt
) {

    public static LostItemArticleResponse of(Article article, Boolean isMine) {
        LostItemArticle lostItemArticle = article.getLostItemArticle();

        return new LostItemArticleResponse(
            article.getId(),
            article.getBoard().getId(),
            lostItemArticle.getType(),
            lostItemArticle.getCategory(),
            lostItemArticle.getFoundPlace(),
            lostItemArticle.getFoundDate(),
            article.getContent(),
            article.getAuthor(),
            lostItemArticle.getIsCouncil(),
            isMine,
            lostItemArticle.getIsFound(),
            lostItemArticle.getImages().stream()
                .map(InnerLostItemImageResponse::from)
                .toList(),
            article.getPrevId(),
            article.getNextId(),
            article.getRegisteredAt(),
            article.getUpdatedAt()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerLostItemImageResponse(
        @Schema(description = "분실물 이미지 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이미지 Url", example = "https://api.koreatech.in/image.jpg\"", requiredMode = REQUIRED)
        String imageUrl
    ) {
        public static InnerLostItemImageResponse from(LostItemImage lostItemImage) {
            return new InnerLostItemImageResponse(
                lostItemImage.getId(),
                lostItemImage.getImageUrl()
            );
        }
    }
}
