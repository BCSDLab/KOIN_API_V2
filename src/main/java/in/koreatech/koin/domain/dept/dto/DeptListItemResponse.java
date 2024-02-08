package in.koreatech.koin.domain.dept.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dept.model.Dept;

@JsonNaming(value = SnakeCaseStrategy.class)
public record DeptListItemResponse(String name, String curriculumLink, List<String> deptNums) {

    public static DeptListItemResponse from(Dept dept) {
        return new DeptListItemResponse(
            dept.getName(),
            dept.getCurriculumLink(),
            dept.getNumbers()
        );
    }
}
