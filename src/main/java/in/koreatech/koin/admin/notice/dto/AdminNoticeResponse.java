package in.koreatech.koin.admin.notice.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

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
    String content
) {

    public static AdminNoticeResponse from(Article article) {
        return new AdminNoticeResponse(
            article.getId(),
            article.getAuthor(),
            article.getTitle(),
            article.getContent()
        );
    }
}
