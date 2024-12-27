package in.koreatech.koin.domain.graduation.dto;

public record ExcelStudentData(
    String excelYear,
    String excelSemester,
    String excelCode,
    String excelClassTitle,
    String excelLectureClass,
    String excelProfessor,
    String excelCourseType,
    String excelCredit,
    String excelGrade,
    String excelRetakeStatus
) {

}
