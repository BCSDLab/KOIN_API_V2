package in.koreatech.koin.domain.graduation.service;

import static in.koreatech.koin.domain.student.util.StudentUtil.parseStudentNumberYear;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.domain.graduation.model.DetectGraduationCalculation;
import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;
import in.koreatech.koin.domain.graduation.repository.DetectGraduationCalculationRepository;
import in.koreatech.koin.domain.graduation.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.graduation.repository.StudentCourseCalculationRepository;
import in.koreatech.koin.domain.student.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.graduation.model.GradeExcelData;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;
import in.koreatech.koin.domain.graduation.exception.ExcelFileCheckException;
import in.koreatech.koin.domain.graduation.exception.ExcelFileNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraduationService {

    private final StudentRepository studentRepository;
    private final StudentCourseCalculationRepository studentCourseCalculationRepository;
    private final StandardGraduationRequirementsRepository standardGraduationRequirementsRepository;
    private final DetectGraduationCalculationRepository detectGraduationCalculationRepository;
    private static final String MIDDLE_TOTAL = "소 계";
    private static final String TOTAL = "합 계";
    private static final String RETAKE = "Y";
    private static final String UNSATISFACTORY = "U";

    private final CourseTypeRepository courseTypeRepository;
    private final UserRepository userRepository;
    private final SemesterRepositoryV2 semesterRepositoryV2;
    private final LectureRepositoryV2 lectureRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;

    @Transactional
    public void createStudentCourseCalculation(Integer userId) {
        Student student = studentRepository.getById(userId);

        validateStudentField(student.getDepartment(), "학과를 추가하세요.");
        validateStudentField(student.getStudentNumber(), "학번을 추가하세요.");

        initializeStudentCourseCalculation(student, student.getDepartment());

        DetectGraduationCalculation detectGraduationCalculation = DetectGraduationCalculation.builder()
            .user(student.getUser())
            .isChanged(false)
            .build();
        detectGraduationCalculationRepository.save(detectGraduationCalculation);
    }

    @Transactional
    public void resetStudentCourseCalculation(Student student, Department newDepartment) {
        // 기존 학생 졸업요건 계산 정보 삭제
        studentCourseCalculationRepository.findByUserId(student.getUser().getId())
            .ifPresent(studentCourseCalculation -> {
                studentCourseCalculationRepository.deleteAllByUserId(student.getUser().getId());
            });

        initializeStudentCourseCalculation(student, newDepartment);

        detectGraduationCalculationRepository.findByUserId(student.getUser().getId())
            .ifPresent(detectGraduationCalculation -> {
                detectGraduationCalculation.updatedIsChanged(true);
            });
    }

    @Transactional
    public void readStudentGradeExcelFile(MultipartFile file, Integer userId) throws IOException {
        checkFiletype(file);

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new HSSFWorkbook(inputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            TimetableFrame graduationFrame = null;
            String currentSemester = "default";

            for (Row row : sheet) {
                GradeExcelData data = extractExcelData(row);
                if (row.getRowNum() == 0 || skipRow(data)) {
                    continue;
                }

                if (data.classTitle().equals(TOTAL)) {
                    break;
                }

                String semester = getKoinSemester(data.semester(), data.year());
                CourseType courseType = courseTypeRepository.findByName(data.courseType()).orElse(null);
                Lecture lecture = lectureRepositoryV2.findBySemesterAndCodeAndLectureClass(semester,
                    data.code(), data.lectureClass()).orElse(null);

                if (!currentSemester.equals(semester)) {
                    currentSemester = semester;
                    graduationFrame = createFrameAboutExcel(userId, currentSemester);
                }

                TimetableLecture timetableLecture = TimetableLecture.builder()
                    .classTitle(data.classTitle())
                    .classTime(lecture != null ? lecture.getClassTime() : null)
                    .professor(data.professor())
                    .grades(data.credit())
                    .isDeleted(false)
                    .lecture(lecture)
                    .timetableFrame(graduationFrame)
                    .courseType(courseType)
                    .build();

                timetableLectureRepositoryV2.save(timetableLecture);
            }
        }
    }

    private TimetableFrame createFrameAboutExcel(Integer userId, String semester) {
        User user = userRepository.getById(userId);
        Semester saveSemester = semesterRepositoryV2.getBySemester(semester);

        List<TimetableFrame> timetableFrameList = timetableFrameRepositoryV2.findAllByUserIdAndSemesterId
            (userId, saveSemester.getId());
        for (TimetableFrame timetableFrame : timetableFrameList) {
            if (timetableFrame.isMain()) {
                timetableFrame.cancelMain();
            }
        }
        TimetableFrame graduationFrame = TimetableFrame.builder()
            .user(user)
            .semester(saveSemester)
            .name("Graduation Frame")
            .isDeleted(false)
            .isMain(true)
            .build();
        timetableFrameRepositoryV2.save(graduationFrame);

        return graduationFrame;
    }

    private GradeExcelData extractExcelData(Row row) {
        return GradeExcelData.fromRow(row);
    }

    private void checkFiletype(MultipartFile file) {
        if (file == null) {
            throw new ExcelFileNotFoundException("파일이 있는지 확인 해주세요.");
        }

        String fileName = file.getOriginalFilename();
        int findDot = fileName.lastIndexOf(".");
        if (findDot == -1) {
            throw new ExcelFileNotFoundException("파일의 형식이 맞는지 확인 해주세요.");
        }

        String extension = fileName.substring(findDot + 1);
        if (!extension.equals("xls") && !extension.equals("xlsx")) {
            throw new ExcelFileCheckException("엑셀 파일인지 확인 해주세요.");
        }
    }

    private boolean skipRow(GradeExcelData gradeExcelData) {
        return gradeExcelData.classTitle().equals(MIDDLE_TOTAL) ||
            gradeExcelData.retakeStatus().equals(RETAKE) ||
            gradeExcelData.grade().equals(UNSATISFACTORY);
    }

    private String getKoinSemester(String semester, String year) {
        if (semester.equals("1") || semester.equals("2")) {
            return year + semester;
        } else if (semester.equals("동계")) {
            return year + "-" + "겨울";
        } else
            return year + "-" + "여름";
    }

    private void validateStudentField(Object field, String message) {
        if (field == null) {
            throw DepartmentNotFoundException.withDetail(message);
        }
    }

    private void initializeStudentCourseCalculation(Student student, Department department) {
        // 학번에 맞는 이수요건 정보 조회
        List<StandardGraduationRequirements> requirementsList =
            standardGraduationRequirementsRepository.findAllByDepartmentAndYear(
                department, student.getStudentNumber().substring(0, 4));

        // 학생 졸업요건 계산 정보 초기화
        requirementsList.forEach(requirement ->
            studentCourseCalculationRepository.save(
                StudentCourseCalculation.builder()
                    .completedGrades(0)
                    .user(student.getUser())
                    .standardGraduationRequirements(requirement)
                    .build()
            )
        );
    }
}
