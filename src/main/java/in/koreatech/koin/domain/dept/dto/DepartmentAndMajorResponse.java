package in.koreatech.koin.domain.dept.dto;

import java.util.List;

import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import io.swagger.v3.oas.annotations.media.Schema;

public record DepartmentAndMajorResponse(
    @Schema(description = "학과", example = "기계공학부")
    String department,

    @Schema(description = "전공 리스트", example = """
        ["친환경자동차･에너지트랙전공", "시스템설계제조전공", "생산시스템전공"]
        """)
    List<String> majors
) {
    public static DepartmentAndMajorResponse of(Department department, List<Major> majorList) {
        return new DepartmentAndMajorResponse(
            department.getName(),
            majorList.stream().map(Major::getName).toList()
        );
    }
}
