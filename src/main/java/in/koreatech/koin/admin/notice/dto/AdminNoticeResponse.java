package in.koreatech.koin.admin.notice.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.model.Article;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminNoticeResponse(
    @Schema(description = "공지사항 글번호", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "공지사항 작성자", requiredMode = REQUIRED)
    String author,

    @Schema(description = "공지사항 제목", requiredMode = REQUIRED)
    String title,

    @Schema(description = "공지사항 본문", requiredMode = REQUIRED)
    String content,

    @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt
) {
    public static AdminNoticeResponse from(Article article) {
        return new AdminNoticeResponse(
            article.getId(),
            article.getAuthor(),
            article.getTitle(),
            article.getContent(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }
}
