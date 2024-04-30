package in.koreatech.koin.domain.dept.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dept.model.Dept;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record DeptResponse(
    @Schema(description = "학과 번호", example = "36", requiredMode = REQUIRED)
    String deptNum,

    @Schema(description = "이름", example = "컴퓨터공학부", requiredMode = REQUIRED)
    String name
) {

    public static DeptResponse from(String findNumber, Dept dept) {
        return new DeptResponse(findNumber, dept.getName());
    }
}
