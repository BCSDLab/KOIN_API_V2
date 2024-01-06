package in.koreatech.koin.domain.dept.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dept.model.Dept;

@JsonNaming(value = SnakeCaseStrategy.class)
public record DeptResponse(Long deptNum, String name) {

    public static DeptResponse from(Long findNumber, Dept dept) {
        return new DeptResponse(findNumber, dept.getName());
    }
}
