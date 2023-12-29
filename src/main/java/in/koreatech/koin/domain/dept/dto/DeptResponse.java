package in.koreatech.koin.domain.dept.dto;

import in.koreatech.koin.domain.dept.domain.DeptNum;

public record DeptResponse(Long dept_num, String name) {

    public static DeptResponse from(DeptNum deptNum) {
        return new DeptResponse(deptNum.getNumber(), deptNum.getDept().getName());
    }
}
