package in.koreatech.koin.domain.dept.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dept.model.DeptNum;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DeptResponse(String deptNum, String name) {

    public static DeptResponse from(DeptNum deptNum) {
        return new DeptResponse(deptNum.getNumber().toString(), deptNum.getDeptInfo().getName());
    }
}
