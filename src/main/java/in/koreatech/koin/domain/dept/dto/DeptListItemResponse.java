package in.koreatech.koin.domain.dept.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dept.model.DeptInfo;
import in.koreatech.koin.domain.dept.model.DeptNum;

@JsonNaming(value = SnakeCaseStrategy.class)
public record DeptListItemResponse(String name, String curriculumLink, List<Long> deptNums) {

    public static DeptListItemResponse from(DeptInfo deptInfo) {
        return new DeptListItemResponse(
            deptInfo.getName(),
            deptInfo.getCurriculumLink(),
            deptInfo.getDeptNums().stream()
                .map(DeptNum::getNumber)
                .toList()
        );
    }
}
