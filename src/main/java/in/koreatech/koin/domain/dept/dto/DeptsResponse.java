package in.koreatech.koin.domain.dept.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dept.model.DeptInfo;
import in.koreatech.koin.domain.dept.model.DeptNum;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DeptsResponse(String name, String curriculumLink, List<Long> deptNums) {

    public static DeptsResponse from(DeptInfo deptInfo) {
        return new DeptsResponse(
            deptInfo.getName(),
            deptInfo.getCurriculumLink(),
            deptInfo.getDeptNums().stream()
                .map(DeptNum::getNumber)
                .toList()
        );
    }
}
