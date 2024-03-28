package in.koreatech.koin.domain.dept.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dept.model.Dept;

@JsonNaming(value = SnakeCaseStrategy.class)
public record DeptsResponse(String name, String curriculumLink, List<?> deptNums) {

    public static DeptsResponse from(Dept dept) {
        return new DeptsResponse(
            dept.getName(),
            dept.getCurriculumLink(),
            dept.getNumbers()
        );
    }
}
