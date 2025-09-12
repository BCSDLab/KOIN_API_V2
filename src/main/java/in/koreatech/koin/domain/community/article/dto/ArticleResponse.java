package in.koreatech.koin.domain.community.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ArticleResponse(

    @Schema(description = "게시글 고유 ID", example = "2", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "게시판 고유 ID", example = "4", requiredMode = REQUIRED)
    Integer boardId,

    @Schema(description = "제목", example = "제목", requiredMode = REQUIRED)
    String title,

    @Schema(description = "내용", example = "내용", requiredMode = REQUIRED)
    String content,

    @Schema(description = "작성자", example = "닉네임", requiredMode = REQUIRED)
    String author,

    @Schema(description = "조회수", example = "1", requiredMode = REQUIRED)
    Integer hit,

    @Schema(description = "공지 원본 url", example = "https://portal.koreatech.ac.kr/ctt/bb/bulletin?b=14&ls=20&ln=1&dm=r&p=33248")
    String url,

    @Schema(description = "첨부 파일")
    List<InnerArticleAttachmentResponse> attachments,

    @Schema(description = "이전 게시글 ID", example = "1")
    Integer prevId,

    @Schema(description = "다음 게시글 ID", example = "3")
    Integer nextId,

    @Schema(description = "등록 일자", example = "2024-08-28", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate registeredAt,

    @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
) {

    public static ArticleResponse from(Article article, String content) {
        return new ArticleResponse(
            article.getId(),
            article.getBoard().getId(),
            article.getTitle(),
            content,
            article.getAuthor(),
            article.getTotalHit(),
            article.getUrl(),
            article.getAttachments().stream()
                .map(InnerArticleAttachmentResponse::from)
                .toList(),
            article.getPrevId(),
            article.getNextId(),
            article.getRegisteredAt(),
            article.getUpdatedAt()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerArticleAttachmentResponse(

        @Schema(description = "파일 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "파일 이름", example = "이미지.png", requiredMode = REQUIRED)
        String name,

        @Schema(description = "파일 url", requiredMode = REQUIRED)
        String url,

        @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {

        public static InnerArticleAttachmentResponse from(ArticleAttachment attachment) {
            return new InnerArticleAttachmentResponse(
                attachment.getId(),
                attachment.getName(),
                attachment.getUrl(),
                attachment.getCreatedAt(),
                attachment.getUpdatedAt()
            );
        }
    }
}
