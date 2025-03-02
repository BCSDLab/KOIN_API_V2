package in.koreatech.koin.admin.notice.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminNoticesResponse(
    @Schema(description = "공지사항 목록")
    List<InnerAdminNoticeResponse> notices,

    @Schema(description = "총 공지사항 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지에 포함된 공지사항 게시글 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "총 페이지 수", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage
) {

    public static AdminNoticesResponse of(Page<Article> pagedResult, Criteria criteria) {
        return new AdminNoticesResponse(
            pagedResult.stream()
                .map(AdminNoticesResponse.InnerAdminNoticeResponse::from)
                .toList(),
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerAdminNoticeResponse(

        @Schema(description = "공지사항 글번호", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "공지사항 제목", requiredMode = REQUIRED)
        String title,

        @Schema(description = "공지사항 작성자", requiredMode = REQUIRED)
        String author,

        @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {

        public static InnerAdminNoticeResponse from(Article article) {
            return new InnerAdminNoticeResponse(
                article.getId(),
                article.getTitle(),
                article.getAuthor(),
                article.getCreatedAt(),
                article.getUpdatedAt()
            );
        }
    }
}
