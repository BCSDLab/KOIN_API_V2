package in.koreatech.koin.domain.dept.dto;

import in.koreatech.koin.domain.dept.domain.Dept;

public record DeptResponse(Long dept_num, String name) {

    public static DeptResponse of(Dept dept, Long deptId) {
        return new DeptResponse(deptId, dept.getName());
    }
}
