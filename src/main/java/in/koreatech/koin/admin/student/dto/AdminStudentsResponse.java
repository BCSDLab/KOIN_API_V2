package in.koreatech.koin.admin.student.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.student.model.Student;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminStudentsResponse(
    Integer currentCount,
    Integer currentPage,
    List<StudentInfo> students,
    Long totalCount,
    Integer totalPage
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record StudentInfo(
        String email,
        Integer id,
        String major,
        String name,
        String nickname,
        String studentNumber
    ) {

        public static StudentInfo fromStudent(Student student) {
            return new StudentInfo(
                student.getUser().getEmail(),
                student.getUser().getId(),
                student.getDepartment() == null ? null : student.getDepartment().getName(),
                student.getUser().getName(),
                student.getUser().getNickname(),
                student.getStudentNumber()
            );
        }
    }

    public static AdminStudentsResponse from(Page<Student> studentsPage) {
        return new AdminStudentsResponse(
            studentsPage.getNumberOfElements(),
            studentsPage.getNumber() + 1,
            studentsPage.getContent().stream().map(StudentInfo::fromStudent).collect(Collectors.toList()),
            studentsPage.getTotalElements(),
            studentsPage.getTotalPages()
        );
    }
}
