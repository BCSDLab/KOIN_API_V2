package in.koreatech.koin.admin.updateversion.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.updateversion.model.UpdateVersionHistory;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.global.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminUpdateHistoryResponse(
    @Schema(description = "조건에 해당하는 총 버전 타입의 수", example = "2", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 버전 타입 중에 현재 페이지에서 조회된 수", example = "2", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 타입을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "버전 정보 리스트", requiredMode = REQUIRED)
    List<InnerAdminUpdateHistoryResponse> versions
) {

    public static AdminUpdateHistoryResponse of(Page<UpdateVersionHistory> pagedResult, Criteria criteria) {
        return new AdminUpdateHistoryResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent().stream()
                .map(InnerAdminUpdateHistoryResponse::from)
                .toList()
        );
    }

    public record InnerAdminUpdateHistoryResponse(
        @Schema(description = "업데이트 버전 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "업데이트 버전 타입", example = "ANDROID", requiredMode = REQUIRED)
        UpdateVersionType type,

        @Schema(description = "업데이트 버전", example = "3.5.0", requiredMode = REQUIRED)
        String version,

        @Schema(description = "업데이트 제목", example = "코인의 새로운 기능 업데이트", requiredMode = REQUIRED)
        String title,

        @Schema(description = "생성일", example = "2021-06-21 13:00:00", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "수정일", example = "2021-06-21", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDateTime updatedAt
    ) {
        public static InnerAdminUpdateHistoryResponse from(UpdateVersionHistory history) {
            return new InnerAdminUpdateHistoryResponse(
                history.getId(),
                history.getType(),
                history.getVersion(),
                history.getTitle(),
                history.getCreatedAt(),
                history.getUpdatedAt()
            );
        }
    }
}
