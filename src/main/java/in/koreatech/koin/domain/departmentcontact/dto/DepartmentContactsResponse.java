package in.koreatech.koin.domain.departmentcontact.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.departmentcontact.enums.DepartmentCategory;
import in.koreatech.koin.domain.departmentcontact.model.DepartmentContact;
import in.koreatech.koin.domain.departmentcontact.model.DepartmentContactDepartment;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DepartmentContactsResponse(
    @Schema(description = "최종 수정 일시", example = "2026-07-14T21:24:22", requiredMode = REQUIRED)
    LocalDateTime updatedAt,

    @Schema(description = "카테고리별 부서 연락처", requiredMode = REQUIRED)
    List<CategoryResponse> categories
) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record CategoryResponse(
        @Schema(description = "카테고리 코드", example = "ACADEMIC", requiredMode = REQUIRED)
        DepartmentCategory category,

        @Schema(description = "카테고리명", example = "학사 / 수업", requiredMode = REQUIRED)
        String categoryName,

        @Schema(description = "부서 목록", requiredMode = REQUIRED)
        List<DepartmentResponse> departments
    ) {

        public static CategoryResponse from(
            DepartmentCategory category,
            List<DepartmentContactDepartment> departments
        ) {
            return new CategoryResponse(
                category,
                category.getDisplayName(),
                departments.stream()
                    .map(DepartmentResponse::from)
                    .toList()
            );
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record DepartmentResponse(
        @Schema(description = "부서명", example = "학사팀", requiredMode = REQUIRED)
        String name,

        @Schema(description = "업무별 연락처", requiredMode = REQUIRED)
        List<ContactResponse> contacts
    ) {

        public static DepartmentResponse from(DepartmentContactDepartment department) {
            return new DepartmentResponse(
                department.getName(),
                department.getContacts().stream()
                    .map(ContactResponse::from)
                    .toList()
            );
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record ContactResponse(
        @Schema(description = "업무", example = "교육과정", requiredMode = REQUIRED)
        String task,

        @Schema(description = "전화번호", example = "041-560-2527", requiredMode = REQUIRED)
        String phoneNumber
    ) {

        public static ContactResponse from(DepartmentContact contact) {
            return new ContactResponse(contact.getTask(), contact.getPhoneNumber());
        }
    }
}
