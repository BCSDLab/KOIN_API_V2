package in.koreatech.koin.domain.departmentcontact.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.departmentcontact.model.DepartmentContact;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DepartmentContactResponse(
    @Schema(description = "부서명", example = "학사팀", requiredMode = REQUIRED)
    String department,

    @Schema(description = "업무", example = "교육과정", requiredMode = REQUIRED)
    String task,

    @Schema(description = "전화번호", example = "041-560-2527", requiredMode = REQUIRED)
    String phoneNumber
) {

    public static DepartmentContactResponse from(DepartmentContact contact) {
        return new DepartmentContactResponse(
            contact.getDepartment(),
            contact.getTask(),
            contact.getPhoneNumber()
        );
    }
}
