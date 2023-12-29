package in.koreatech.koin.domain.dept.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dept.domain.Dept;
import in.koreatech.koin.domain.dept.domain.DeptNum;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DeptsResponse(String name, String curriculumLink, List<String> deptNums) {

    public static DeptsResponse from(Dept dept) {
        return new DeptsResponse(
            dept.getName(),
            dept.getCurriculumLink(),
            dept.getDeptNums().stream()
                .map(DeptNum::getNumber)
                .map(String::valueOf)
                .toList()
        );
    }
}
