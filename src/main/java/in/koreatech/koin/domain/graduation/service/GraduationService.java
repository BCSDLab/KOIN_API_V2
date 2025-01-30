package in.koreatech.koin.domain.graduation.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.domain.graduation.dto.CourseTypeLectureResponse;
import in.koreatech.koin.domain.graduation.dto.GraduationCourseCalculationResponse;
import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.DetectGraduationCalculation;
import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.graduation.repository.DetectGraduationCalculationRepository;
import in.koreatech.koin.domain.graduation.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.graduation.repository.StudentCourseCalculationRepository;
import in.koreatech.koin.domain.student.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.student.exception.StudentNumberNotFoundException;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
import in.koreatech.koin.domain.timetableV2.exception.NotFoundSemesterAndCourseTypeException;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;

import in.koreatech.koin.domain.graduation.model.GradeExcelData;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;
import in.koreatech.koin.domain.graduation.exception.ExcelFileCheckException;
import in.koreatech.koin.domain.graduation.exception.ExcelFileNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;
import in.koreatech.koin.domain.timetableV3.service.SemesterServiceV3;
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
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final DetectGraduationCalculationRepository detectGraduationCalculationRepository;
    private final CourseTypeRepository courseTypeRepository;
    private final UserRepository userRepository;
    private final SemesterRepositoryV2 semesterRepositoryV2;
    private final LectureRepositoryV2 lectureRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final SemesterRepositoryV3 semesterRepositoryV3;
    private final CatalogRepository catalogRepository;

    private static final String MIDDLE_TOTAL = "소 계";
    private static final String TOTAL = "합 계";
    private static final String RETAKE = "Y";
    private static final String UNSATISFACTORY = "U";
    private final SemesterServiceV3 semesterServiceV3;

    @Transactional
    public void createStudentCourseCalculation(Integer userId) {
        Student student = studentRepository.getById(userId);

        validateStudentField(student.getMajor(), "전공을 추가하세요.");
        validateStudentField(student.getStudentNumber(), "학번을 추가하세요.");

        initializeStudentCourseCalculation(student, student.getMajor());

        DetectGraduationCalculation detectGraduationCalculation = DetectGraduationCalculation.builder()
            .user(student.getUser())
            .isChanged(false)
            .build();
        detectGraduationCalculationRepository.save(detectGraduationCalculation);
    }

    @Transactional
    public void resetStudentCourseCalculation(Student student, Major newMajor) {
        // 기존 학생 졸업요건 계산 정보 삭제
        studentCourseCalculationRepository.findByUserId(student.getUser().getId())
            .ifPresent(studentCourseCalculation -> {
                studentCourseCalculationRepository.deleteAllByUserId(student.getUser().getId());
            });

        initializeStudentCourseCalculation(student, newMajor);

        detectGraduationCalculationRepository.findByUserId(student.getUser().getId())
            .ifPresent(detectGraduationCalculation -> {
                detectGraduationCalculation.updatedIsChanged(true);
            });
    }

    @Transactional
    public GraduationCourseCalculationResponse getGraduationCourseCalculationResponse(Integer userId) {
        DetectGraduationCalculation detectGraduationCalculation = detectGraduationCalculationRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 GraduationCalculation 정보가 존재하지 않습니다."));

        if (!detectGraduationCalculation.isChanged()) {
            return GraduationCourseCalculationResponse.of(List.of());
        }

        // 학생 정보와 학과 검증
        Student student = getValidatedStudent(userId);
        String studentYear = StudentUtil.parseStudentNumberYearAsString(student.getStudentNumber());

        // 시간표와 대학 요람 데이터 가져오기
        List<Catalog> catalogList = getCatalogListForStudent(student, studentYear);

        // courseTypeId와 학점 맵핑
        Map<Integer, Integer> courseTypeCreditsMap = calculateCourseTypeCredits(catalogList);

        // GraduationRequirements 리스트 조회
        List<StandardGraduationRequirements> graduationRequirements = getGraduationRequirements(catalogList,
            studentYear);

        // 계산 로직 및 응답 생성
        List<GraduationCourseCalculationResponse.InnerCalculationResponse> courseTypes = processGraduationCalculations(
            userId, student, graduationRequirements, courseTypeCreditsMap
        );

        detectGraduationCalculation.updatedIsChanged(false);

        return GraduationCourseCalculationResponse.of(courseTypes);
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
                CourseType courseType = mappingCourseType(data.courseType());
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

    public CourseType mappingCourseType(String courseTypeName) {
        if ("전필".equals(courseTypeName)) {
            return courseTypeRepository.getByName("학과(전공)필수");
        } else if ("전선".equals(courseTypeName)) {
            return courseTypeRepository.getByName("학과(전공)선택");
        } else {
            return courseTypeRepository.findByName(courseTypeName).orElse(null);
        }
    }

    private void initializeStudentCourseCalculation(Student student, Major major) {
        // 학번에 맞는 이수요건 정보 조회
        List<StandardGraduationRequirements> requirementsList =
            standardGraduationRequirementsRepository.findAllByMajorAndYear(
                major, student.getStudentNumber().substring(0, 4));

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

    private Student getValidatedStudent(Integer userId) {
        Student student = studentRepository.getById(userId);

        if (student.getDepartment() == null) {
            throw new DepartmentNotFoundException("학과를 추가하세요.");
        }
        if (student.getStudentNumber() == null) {
            throw new StudentNumberNotFoundException("학번을 추가하세요.");
        }
        return student;
    }

    private List<Catalog> getCatalogListForStudent(Student student, String studentYear) {
        List<TimetableLecture> timetableLectures = timetableFrameRepositoryV2.getAllByUserId(student.getId()).stream()
            .flatMap(frame -> frame.getTimetableLectures().stream())
            .toList();

        List<Catalog> catalogList = new ArrayList<>();
        timetableLectures.forEach(timetableLecture -> {
            String lectureName = timetableLecture.getLecture() != null
                ? timetableLecture.getLecture().getName()
                : timetableLecture.getClassTitle();

            if (lectureName != null) {
                // 1. Major 기반 조회
                List<Catalog> catalogs = catalogRepository.findByLectureNameAndMajorIdAndYear(
                    lectureName, student.getMajor() != null ? student.getMajor().getId() : null, studentYear);

                // 2. 전공에서 못 찾으면 Department 기반 조회
                if (catalogs.isEmpty()) {
                    catalogs = catalogRepository.findByLectureNameAndDepartmentIdAndYear(
                        lectureName, student.getDepartment().getId(), studentYear);
                }

                catalogList.addAll(catalogs);
            }
        });
        return catalogList;
    }

    private Map<Integer, Integer> calculateCourseTypeCredits(List<Catalog> catalogList) {
        Map<Integer, Integer> courseTypeCreditsMap = new HashMap<>();
        for (Catalog catalog : catalogList) {
            int courseTypeId = catalog.getCourseType().getId();
            courseTypeCreditsMap.put(courseTypeId,
                courseTypeCreditsMap.getOrDefault(courseTypeId, 0) + catalog.getCredit());
        }
        return courseTypeCreditsMap;
    }

    private List<StandardGraduationRequirements> getGraduationRequirements(List<Catalog> catalogList,
        String studentYear) {
        return catalogList.stream()
            .map(catalog -> standardGraduationRequirementsRepository.findByMajorIdAndCourseTypeIdAndYear(
                catalog.getMajor().getId(),
                catalog.getCourseType().getId(),
                studentYear
            ))
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    private List<GraduationCourseCalculationResponse.InnerCalculationResponse> processGraduationCalculations(
        Integer userId, Student student, List<StandardGraduationRequirements> graduationRequirements,
        Map<Integer, Integer> courseTypeCreditsMap) {

        return graduationRequirements.stream()
            .map(requirement -> GraduationCourseCalculationResponse.InnerCalculationResponse.of(
                requirement.getCourseType().getName(),
                requirement.getRequiredGrades(),
                updateStudentCourseCalculation(userId, student, requirement, courseTypeCreditsMap)
            ))
            .toList();
    }

    private int updateStudentCourseCalculation(Integer userId, Student student,
        StandardGraduationRequirements requirement,
        Map<Integer, Integer> courseTypeCreditsMap) {
        if (requirement.getCourseType() == null) {
            return 0;
        }

        int completedGrades = courseTypeCreditsMap.getOrDefault(requirement.getCourseType().getId(), 0);

        StudentCourseCalculation existingCalculation = studentCourseCalculationRepository
            .findByUserIdAndStandardGraduationRequirementsId(userId, requirement.getId());

        if (existingCalculation != null) {
            completedGrades += existingCalculation.getCompletedGrades();
            studentCourseCalculationRepository.delete(existingCalculation);
        }

        StudentCourseCalculation newCalculation = StudentCourseCalculation.builder()
            .completedGrades(completedGrades)
            .user(student.getUser())
            .standardGraduationRequirements(requirement)
            .build();
        studentCourseCalculationRepository.save(newCalculation);

        return completedGrades;
    }

    public CourseTypeLectureResponse getLectureByCourseType(Integer year, String term, String courseTypeName) {
        CourseType courseType = courseTypeRepository.getByName(courseTypeName);
        List<Catalog> catalogs = catalogRepository.getAllByCourseTypeId(courseType.getId());
        List<String> codes = catalogs.stream().map(Catalog::getCode).toList();

        Term parsedTerm = Term.fromDescription(term);
        Semester foundSemester = semesterRepositoryV3.getByYearAndTerm(year, parsedTerm);
        String semester = foundSemester.getSemester();

        List<Lecture> lectures = lectureRepositoryV2.findAllByCodesAndSemester(codes, semester)
            .orElseThrow(() -> new NotFoundSemesterAndCourseTypeException("학기나 이수구분을 찾을 수 없습니다."));

        return CourseTypeLectureResponse.of(semester, lectures);
    }
}
