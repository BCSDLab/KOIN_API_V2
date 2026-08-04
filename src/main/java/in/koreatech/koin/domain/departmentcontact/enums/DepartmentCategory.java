package in.koreatech.koin.domain.departmentcontact.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DepartmentCategory {
    ACADEMIC("학사 / 수업"),
    STUDENT_SUPPORT("학생지원 / 행정"),
    EMPLOYMENT("취업 / 현장실습"),
    INTERNATIONAL("국제 / 교환학생"),
    FACILITY("시설 / 생활"),
    OTHER("기타 기관")
    ;

    private final String displayName;
}
