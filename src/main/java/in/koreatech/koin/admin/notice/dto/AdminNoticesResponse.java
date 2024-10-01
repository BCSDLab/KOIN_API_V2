package in.koreatech.koin.admin.notice.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminNoticesResponse (
    @Schema(description = "공지사항 목록")
    List<AdminNoticesResponse.InnerAdminNoticeResponse> notices,

    @Schema(description = "총 공지사항 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지에 포함된 공지사항 게시글 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "총 페이지 수", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage
) {

    public static AdminNoticesResponse of(Page<Void> pagedResult, Criteria criteria) {
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
        String author
    ) {

        public static AdminNoticesResponse.InnerAdminNoticeResponse from(Object notice) {
            return null;
        }
    }
}
