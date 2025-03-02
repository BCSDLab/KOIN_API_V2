package in.koreatech.koin.admin.version.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminVersionHistoryResponse(
    @Schema(description = "조건에 해당하는 총 버전 타입의 수", example = "2", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 버전 타입 중에 현재 페이지에서 조회된 수", example = "2", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 타입을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "버전 정보 리스트", requiredMode = REQUIRED)
    List<InnerAdminVersionHistoryResponse> versions
) {

    public static AdminVersionHistoryResponse of(Page<Version> pagedResult, Criteria criteria) {
        return new AdminVersionHistoryResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent().stream()
                .map(InnerAdminVersionHistoryResponse::from)
                .toList()
        );
    }

    public record InnerAdminVersionHistoryResponse(
        @Schema(description = "업데이트 버전 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "업데이트 버전 타입", example = "ANDROID", requiredMode = REQUIRED)
        String type,

        @Schema(description = "업데이트 버전", example = "3.5.0", requiredMode = REQUIRED)
        String version,

        @Schema(description = "업데이트 제목", example = "코인의 새로운 기능 업데이트", requiredMode = REQUIRED)
        String title,

        @Schema(description = "업데이트 버전 내용", requiredMode = REQUIRED)
        String content,

        @Schema(description = "생성일", example = "2021-06-21 13:00:00", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "수정일", example = "2021-06-21", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDateTime updatedAt
    ) {
        public static InnerAdminVersionHistoryResponse from(Version history) {
            return new InnerAdminVersionHistoryResponse(
                history.getId(),
                history.getType(),
                history.getVersion(),
                history.getTitle(),
                history.getContent(),
                history.getCreatedAt(),
                history.getUpdatedAt()
            );
        }
    }
}
