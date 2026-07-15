package in.koreatech.koin.domain.departmentcontact.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.departmentcontact.dto.DepartmentContactsResponse.DepartmentResponse;
import in.koreatech.koin.domain.departmentcontact.enums.DepartmentCategory;
import in.koreatech.koin.domain.departmentcontact.model.DepartmentContactDepartment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DepartmentCategoryContactsResponse(
    @Schema(description = "최종 수정 일시", example = "2026-07-14T21:24:22", requiredMode = REQUIRED)
    LocalDateTime updatedAt,

    @Schema(description = "카테고리 코드", example = "EMPLOYMENT", requiredMode = REQUIRED)
    DepartmentCategory category,

    @Schema(description = "카테고리명", example = "취업 / 현장실습", requiredMode = REQUIRED)
    String categoryName,

    @Schema(description = "부서 목록", requiredMode = REQUIRED)
    List<DepartmentResponse> departments
) {

    public static DepartmentCategoryContactsResponse from(
        LocalDateTime updatedAt,
        DepartmentCategory category,
        List<DepartmentContactDepartment> departments
    ) {
        return new DepartmentCategoryContactsResponse(
            updatedAt,
            category,
            category.getDisplayName(),
            departments.stream()
                .map(DepartmentResponse::from)
                .toList()
        );
    }
}
