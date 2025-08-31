package in.koreatech.koin.domain.graduation.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.domain.graduation.dto.CourseTypeLectureResponse;
import in.koreatech.koin.domain.graduation.dto.GeneralEducationLectureResponse;
import in.koreatech.koin.domain.graduation.dto.GraduationCourseCalculationResponse;
import in.koreatech.koin.domain.graduation.enums.GeneralEducationAreaEnum;
import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.CatalogResult;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.model.DetectGraduationCalculation;
import in.koreatech.koin.domain.graduation.model.GeneralEducationArea;
import in.koreatech.koin.domain.graduation.model.GradeExcelData;
import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;
import in.koreatech.koin.domain.graduation.repository.DetectGraduationCalculationRepository;
import in.koreatech.koin.domain.graduation.repository.GeneralEducationAreaRepository;
import in.koreatech.koin.domain.graduation.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.graduation.repository.StudentCourseCalculationRepository;
import in.koreatech.koin.domain.student.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.student.exception.StudentNumberNotFoundException;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.exception.NotFoundSemesterAndCourseTypeException;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.global.exception.custom.DuplicationException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraduationService {

    private final EntityManager entityManager;
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
    private final GeneralEducationAreaRepository generalEducationAreaRepository;
    private final GraduationExcelService graduationExcelService;

    private static final String MIDDLE_TOTAL = "소 계";
    private static final String TOTAL = "합 계";
    private static final String FAIL = "F";
    private static final String UNSATISFACTORY = "U";
    private static final String DEFAULT_COURSER_TYPE = "이수구분선택";
    private static final String GENERAL_EDUCATION_COURSE_TYPE = "교양선택";
    private static final Integer SELECTIVE_EDUCATION_REQUIRED_CREDIT = 3;

    @ConcurrencyGuard(lockName = "createCalculation")
    public void createStudentCourseCalculation(Integer userId) {
        Student student = studentRepository.getById(userId);

        validateGraduationCalculatedDataExist(userId);
        validateStudentField(student.getMajor(), "전공을 추가하세요.");
        validateStudentField(student.getStudentNumber(), "학번을 추가하세요.");

        initializeStudentCourseCalculation(student, student.getMajor());

        DetectGraduationCalculation detectGraduationCalculation = DetectGraduationCalculation.builder()
            .user(student.getUser())
            .isChanged(false)
            .build();
        detectGraduationCalculationRepository.save(detectGraduationCalculation);
    }

    private void validateGraduationCalculatedDataExist(Integer userId) {
        detectGraduationCalculationRepository.findByUserId(userId)
            .ifPresent(detectGraduationCalculation -> {
                throw new DuplicationException("이미 졸업요건 계산이 초기화 되었습니다.") {
                };
            });
        if (!studentCourseCalculationRepository.findAllByUserId(userId).isEmpty()) {
            throw new DuplicationException("이미 졸업요건 계산이 초기화 되었습니다.") {
            };
        }

    }

    private void validateStudentField(Object field, String message) {
        if (field == null) {
            throw DepartmentNotFoundException.withDetail(message);
        }
    }

    @Transactional
    public void resetStudentCourseCalculation(Student student, Major newMajor) {
        // 기존 학생 졸업요건 계산 정보 삭제
        if (!studentCourseCalculationRepository.findAllByUserId(student.getUser().getId()).isEmpty()) {
            studentCourseCalculationRepository.deleteAllByUserId(student.getUser().getId());
            entityManager.flush();
            entityManager.clear();
            initializeStudentCourseCalculation(student, newMajor);

            detectGraduationCalculationRepository.findByUserId(student.getUser().getId())
                .ifPresent(detectGraduationCalculation -> detectGraduationCalculation.updatedIsChanged(true));
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

    @Transactional
    public GraduationCourseCalculationResponse getGraduationCourseCalculationResponse(Integer userId) {
        DetectGraduationCalculation detectGraduationCalculation = detectGraduationCalculationRepository.findByUserId(
                userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 GraduationCalculation 정보가 존재하지 않습니다."));

        if (!detectGraduationCalculation.isChanged()) {
            return getExistingGraduationCalculation(userId);
        }

        Student student = getValidatedStudent(userId);
        String studentYear = StudentUtil.parseStudentNumberYearAsString(student.getStudentNumber());

        List<Catalog> catalogList = getCatalogListForStudent(student, studentYear);
        Map<Integer, Integer> courseTypeCreditsMap = calculateCourseTypeCredits(catalogList, student);

        List<GraduationCourseCalculationResponse.InnerCalculationResponse> courseTypes = processGraduationCalculations(
            student, courseTypeCreditsMap
        );

        courseTypes.sort(
            Comparator.comparing(GraduationCourseCalculationResponse.InnerCalculationResponse::courseType));

        detectGraduationCalculation.updatedIsChanged(false);

        return GraduationCourseCalculationResponse.of(courseTypes);
    }

    private GraduationCourseCalculationResponse getExistingGraduationCalculation(Integer userId) {
        List<StudentCourseCalculation> existingCalculations = studentCourseCalculationRepository.findAllByUserId(
            userId);

        List<GraduationCourseCalculationResponse.InnerCalculationResponse> courseTypes = existingCalculations.stream()
            .map(calc -> {
                StandardGraduationRequirements requirement = calc.getStandardGraduationRequirements();
                return GraduationCourseCalculationResponse.InnerCalculationResponse.of(
                    requirement.getCourseType().getName(),
                    requirement.getRequiredGrades(),
                    calc.getCompletedGrades()
                );
            })
            .sorted(Comparator.comparing(GraduationCourseCalculationResponse.InnerCalculationResponse::courseType))
            .collect(Collectors.toList());

        return GraduationCourseCalculationResponse.of(courseTypes);
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
        List<TimetableLecture> timetableLectures = timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(student.getId())
            .stream()
            .flatMap(frame -> frame.getTimetableLectures().stream())
            .toList();

        List<Catalog> catalogList = new ArrayList<>();
        for (TimetableLecture timetableLecture : timetableLectures) {
            String lectureName = timetableLecture.getLecture() != null
                ? timetableLecture.getLecture().getName()
                : timetableLecture.getClassTitle();

            if (lectureName != null) {
                Catalog bestCatalog =
                    findBestMatchingCatalog(lectureName, studentYear, student.getMajor(), timetableLecture);

                if (bestCatalog != null) {
                    if (timetableLecture.getCourseType() != null) {
                        bestCatalog = Catalog.builder()
                            .year(bestCatalog.getYear())
                            .code(bestCatalog.getCode())
                            .lectureName(bestCatalog.getLectureName())
                            .credit(bestCatalog.getCredit())
                            .major(bestCatalog.getMajor())
                            .department(bestCatalog.getDepartment())
                            .courseType(timetableLecture.getCourseType())
                            .generalEducationArea(bestCatalog.getGeneralEducationArea())
                            .build();
                    }
                    catalogList.add(bestCatalog);
                }
            }
        }
        return catalogList;
    }

    private Catalog findBestMatchingCatalog(
        String lectureName,
        String studentYear,
        Major major,
        TimetableLecture timetableLecture) {
        List<Catalog> catalogs = catalogRepository.findByLectureNameAndYearAndMajor(lectureName, studentYear, major);
        if (!catalogs.isEmpty()) {
            return catalogs.get(0);
        }

        List<String> attendedYears = timetableLectureRepositoryV2.findYearsByUserId(major.getId());
        catalogs = catalogRepository.findByLectureNameAndYearIn(lectureName, attendedYears);
        if (!catalogs.isEmpty()) {
            return catalogs.get(0);
        }

        catalogs = catalogRepository.findByLectureNameOrderByYearDesc(lectureName);
        if (!catalogs.isEmpty()) {
            return catalogs.get(0);
        }

        CourseType defaultCourseType = courseTypeRepository.getByName(DEFAULT_COURSER_TYPE);
        return Catalog.builder()
            .year(studentYear)
            .code(timetableLecture.getLecture() != null ? timetableLecture.getLecture().getCode() : "UNKNOWN")
            .lectureName(lectureName)
            .credit(
                Integer.parseInt(timetableLecture.getLecture() != null ? timetableLecture.getLecture().getGrades() : timetableLecture.getGrades()))
            .major(major)
            .department(null)
            .courseType(defaultCourseType)
            .generalEducationArea(null)
            .build();
    }

    private Map<Integer, Integer> calculateCourseTypeCredits(List<Catalog> catalogList, Student student) {
        Map<Integer, Integer> courseTypeCreditsMap = new HashMap<>();

        List<TimetableLecture> timetableLectures = timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(student.getId())
            .stream()
            .flatMap(frame -> frame.getTimetableLectures().stream())
            .toList();

        for (TimetableLecture timetableLecture : timetableLectures) {
            Lecture lecture = timetableLecture.getLecture();
            String lectureName = (lecture != null) ? lecture.getName() : timetableLecture.getClassTitle();

            Catalog matchingCatalog = catalogList.stream()
                .filter(catalog -> catalog.getLectureName().equals(lectureName))
                .findFirst()
                .orElse(null);

            if (matchingCatalog == null) {
                CourseType defaultCourseType = courseTypeRepository.getByName(DEFAULT_COURSER_TYPE);
                matchingCatalog = Catalog.builder()
                    .year(StudentUtil.parseStudentNumberYearAsString(student.getStudentNumber()))
                    .code("UNKNOWN")
                    .lectureName(lectureName)
                    .credit(0)
                    .major(student.getMajor())
                    .department(null)
                    .courseType(defaultCourseType)
                    .generalEducationArea(null)
                    .build();
            }

            CourseType appliedCourseType = matchingCatalog.getCourseType();

            if (timetableLecture.getCourseType() != null) {
                appliedCourseType = timetableLecture.getCourseType();
            }

            String grades = (lecture != null) ? lecture.getGrades() : timetableLecture.getGrades();
            int credit = Integer.parseInt(grades);

            int courseTypeId = appliedCourseType.getId();
            courseTypeCreditsMap.put(courseTypeId, courseTypeCreditsMap.getOrDefault(courseTypeId, 0) + credit);
        }

        return courseTypeCreditsMap;
    }

    private List<GraduationCourseCalculationResponse.InnerCalculationResponse> processGraduationCalculations(
        Student student, Map<Integer, Integer> courseTypeCreditsMap) {

        List<StandardGraduationRequirements> allRequirements =
            standardGraduationRequirementsRepository.findAllByMajorAndYear(student.getMajor(),
                StudentUtil.parseStudentNumberYearAsString(student.getStudentNumber()));

        Map<CourseType, Integer> groupedRequirements = new HashMap<>();

        for (StandardGraduationRequirements requirement : allRequirements) {
            int requiredGrades = requirement.getRequiredGrades();
            int completedGrades = courseTypeCreditsMap.getOrDefault(requirement.getCourseType().getId(), 0);

            groupedRequirements.put(requirement.getCourseType(), requiredGrades);

            updateStudentCourseCalculation(student, requirement.getCourseType(), completedGrades);
        }

        List<GraduationCourseCalculationResponse.InnerCalculationResponse> results = new ArrayList<>();

        for (Map.Entry<CourseType, Integer> entry : groupedRequirements.entrySet()) {
            CourseType courseType = entry.getKey();
            int totalRequiredGrades = entry.getValue();
            int completedGrades = courseTypeCreditsMap.getOrDefault(courseType.getId(), 0);

            results.add(GraduationCourseCalculationResponse.InnerCalculationResponse.of(
                courseType.getName(), totalRequiredGrades, completedGrades
            ));
        }

        return results;
    }

    private void updateStudentCourseCalculation(Student student, CourseType courseType, int completedGrades) {
        String studentYear = StudentUtil.parseStudentNumberYearAsString(student.getStudentNumber());

        Optional<StandardGraduationRequirements> standardGraduationRequirementOpt =
            standardGraduationRequirementsRepository.findFirstByMajorIdAndCourseTypeIdAndYear(
                student.getMajor().getId(),
                courseType.getId(),
                studentYear
            );

        if (standardGraduationRequirementOpt.isEmpty()) {
            return;
        }

        Optional<StudentCourseCalculation> existingCalculation = studentCourseCalculationRepository
            .findByUserIdAndStandardGraduationRequirements(student.getId(), standardGraduationRequirementOpt.get());

        if (existingCalculation.isPresent()) {
            if (existingCalculation.get().getCompletedGrades() != completedGrades) {
                existingCalculation.get().updateCompletedGrades(completedGrades);
                studentCourseCalculationRepository.save(existingCalculation.get());
            }
        } else {
            StudentCourseCalculation newCalculation = StudentCourseCalculation.builder()
                .completedGrades(completedGrades)
                .isDeleted(false)
                .user(student.getUser())
                .standardGraduationRequirements(standardGraduationRequirementOpt.get())
                .build();
            studentCourseCalculationRepository.save(newCalculation);
        }
    }

    @Transactional
    public void readStudentGradeExcelFile(MultipartFile file, Integer userId) throws IOException {
        Student student = studentRepository.getById(userId);
        String studentYear = StudentUtil.parseStudentNumberYearAsString(student.getStudentNumber());

        List<GradeExcelData> gradeExcelData = graduationExcelService.parseStudentGradeFromExcel(file);
        Set<String> lectureNames = new HashSet<>(gradeExcelData.stream()
            .map(GradeExcelData::classTitle)
            .toList());
        Set<String> lectureCodes = new HashSet<>(gradeExcelData.stream()
            .map(GradeExcelData::code)
            .toList());
        Set<String> semesters = new HashSet<>(gradeExcelData.stream()
            .map(GradeExcelData::semester)
            .toList());
        Set<String> years = new HashSet<>(gradeExcelData.stream()
            .map(GradeExcelData::year)
            .toList());
        List<TimetableLecture> timetableLectures = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new HSSFWorkbook(inputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            TimetableFrame graduationFrame = null;
            String currentSemester = "default";

            Map<String, List<Lecture>> lectureMap = loadLectures(semesters, lectureCodes);
            Map<String, Catalog> catalogByNameMap = loadCatalogByLectureName(lectureNames, studentYear);
            Map<String, Catalog> catalogByCodeMap = loadCatalogByCode(lectureCodes, years);
            /*
                이수구분선택이란? : 대학요람(Catalog)에 없어서 매핑되지 않는 과목들의 기본 매핑입니다.
                현재로서 파악된 것은 K-MOOC / 단기, 장기현장실습 / 2019-1학기 과목들이 매핑됩니다.
            */
            for (Row row : sheet) {
                GradeExcelData data = extractExcelData(row);
                if (row.getRowNum() == 0 || skipRow(data)) {
                    continue;
                }
                if (data.classTitle().equals(TOTAL)) {
                    break;
                }

                String semester = getKoinSemester(data.semester(), data.year());

                List<Lecture> lectures = lectureMap.get(semester + "_" + data.code());
                Lecture lecture = findBestMatchingLecture(lectures, data.lectureClass());

                CatalogResult catalogResult = findCourseType(lecture, data, studentYear, catalogByNameMap,
                    catalogByCodeMap);
                CourseType courseType = catalogResult.courseType();
                GeneralEducationArea generalEducationArea = catalogResult.generalEducation();

                graduationFrame = updateAndCreateFrame(userId, semester, currentSemester, graduationFrame);
                currentSemester = semester;

                timetableLectures.add(
                    createTimetableLecture(data, lecture, graduationFrame, courseType, generalEducationArea));
            }
            timetableLectureRepositoryV2.saveAll(timetableLectures);
        }
    }

    private GradeExcelData extractExcelData(Row row) {
        return GradeExcelData.fromRow(row);
    }

    private boolean skipRow(GradeExcelData gradeExcelData) {
        return gradeExcelData.classTitle().equals(MIDDLE_TOTAL) ||
            gradeExcelData.grade().equals(FAIL) ||
            gradeExcelData.grade().equals(UNSATISFACTORY);
    }

    // 분반 문제를 해결하기 위해서, 강의들을 전부 가져오도록 했음
    private Map<String, List<Lecture>> loadLectures(Set<String> semesters, Set<String> lectureCodes) {
        return lectureRepositoryV2.findAllBySemesterInAndCodeIn(semesters, lectureCodes)
            .stream().collect(Collectors.groupingBy(l -> l.getSemester() + "_" + l.getCode()));
    }

    // 1차 탐색 요소, 학번의 연도와 수업 이름으로 카탈로그를 가져옴
    private Map<String, Catalog> loadCatalogByLectureName(Set<String> lectureNames, String year) {
        return catalogRepository.findAllByLectureNameInAndYear(lectureNames, year).stream().collect(Collectors.
            toMap(c -> c.getLectureName() + "_" + c.getYear(), Function.identity(), (existing, duplicate) -> duplicate
            ));
    }

    // 2차 탐색 요소, 1차에서 찾지 못했을 경우 학생이 지금까지 다닌 모든 학기(연도)와 코드를 통해서 카탈로그를 가져옴
    private Map<String, Catalog> loadCatalogByCode(Set<String> lectureCodes, Set<String> years) {
        return catalogRepository.findAllByCodeInAndYearIn(lectureCodes, years).stream().collect(Collectors.
            toMap(c -> c.getCode() + "_" + c.getYear(), Function.identity(), (existing, duplicate) -> duplicate
            ));
    }

    private String getKoinSemester(String semester, String year) {
        if (semester.equals("1") || semester.equals("2")) {
            return year + semester;
        } else if (semester.equals("동계")) {
            return year + "-" + "겨울";
        } else
            return year + "-" + "여름";
    }

    private Lecture findBestMatchingLecture(List<Lecture> lectures, String lectureClass) {
        if (lectures == null || lectures.isEmpty())
            return null;
        for (Lecture lecture : lectures) {
            if (lecture.getLectureClass().equals(lectureClass)) {
                return lecture;
            }
        }
        return lectures.get(0);
    }

    public CatalogResult findCourseType(Lecture lecture, GradeExcelData data,
        String studentYear, Map<String, Catalog> catalogByNameMap, Map<String, Catalog> catalogByCodeMap) {

        // 자유선택은 학생이 직접 신청하는 부분이라 매핑해줬음
        if (data.courseType().equals("자선")) {
            return new CatalogResult(courseTypeRepository.getByName(DEFAULT_COURSER_TYPE), null);
        }

        if (lecture == null) {
            return new CatalogResult(courseTypeRepository.getByName(DEFAULT_COURSER_TYPE), null);
        }
        /*
            Name과 Code로 나눈 이유? : 1차로는 학생의 학번과 수업이름으로 찾습니다.(코드가 다른 경우가 많음)
            2차로는 1차에서 찾지 못 한 수업을 연도를 찾아가며 코드를 비교해서 찾습니다.(학생 학번에 없는 수업 수강 찾기)
        */
        Catalog catalog = catalogByNameMap.get(data.classTitle() + "_" + studentYear);
        if (catalog == null) {
            catalog = catalogByCodeMap.get(data.code() + "_" + data.year());
        }

        // catalog가 없으면 기본 이수구분 반환
        if (catalog == null) {
            return new CatalogResult(courseTypeRepository.getByName(DEFAULT_COURSER_TYPE), null);
        }

        // CourseType과 GeneralEducation_id 반환
        return new CatalogResult(catalog.getCourseType(), catalog.getGeneralEducationArea());
    }

    private TimetableFrame updateAndCreateFrame(Integer userId, String semester,
        String currentSemester, TimetableFrame graduationFrame) {
        if (!currentSemester.equals(semester)) {
            User user = userRepository.getById(userId);
            Semester saveSemester = semesterRepositoryV2.getBySemester(semester);

            timetableFrameRepositoryV2.findAllByUserIdAndSemesterId(userId, saveSemester.getId())
                .stream().filter(TimetableFrame::isMain)
                .forEach(frame -> {
                    frame.cancelMain();
                    timetableFrameRepositoryV2.save(frame);
                });

            return timetableFrameRepositoryV2.save(
                TimetableFrame.builder()
                    .user(user)
                    .semester(saveSemester)
                    .name("졸업학점 계산 테이블")
                    .isDeleted(false)
                    .isMain(true)
                    .build()
            );
        }
        return graduationFrame;
    }

    private TimetableLecture createTimetableLecture(GradeExcelData data, Lecture lecture,
        TimetableFrame graduationFrame, CourseType courseType, GeneralEducationArea generalEducationArea) {
        return TimetableLecture.builder()
            .classTitle(data.classTitle())
            .classTime(lecture != null ? lecture.getClassTime() : "[]")
            .professor(data.professor())
            .grades(data.credit())
            .isDeleted(false)
            .lecture(lecture)
            .timetableFrame(graduationFrame)
            .courseType(courseType)
            .generalEducationArea(generalEducationArea)
            .build();
    }

    public CourseTypeLectureResponse getLectureByCourseType(Integer year, String term, String courseTypeName,
        String generalEducationAreaName) {
        CourseType courseType = courseTypeRepository.getByName(courseTypeName);

        List<Catalog> catalogs = catalogRepository.getAllByCourseTypeId(courseType.getId());

        if (generalEducationAreaName != null) {
            if (generalEducationAreaName.equals("교양선택")) {
                catalogs = catalogs.stream()
                    .filter(catalog -> catalog.getGeneralEducationArea() == null)
                    .toList();
            } else {
                GeneralEducationArea generalEducationArea =
                    generalEducationAreaRepository.getGeneralEducationAreaByName(generalEducationAreaName);

                catalogs = catalogs.stream()
                    .filter(catalog -> catalog.getGeneralEducationArea() != null && catalog.getGeneralEducationArea()
                        .getId()
                        .equals(generalEducationArea.getId()))
                    .toList();
            }
        }

        List<String> codes = catalogs.stream().map(Catalog::getCode).toList();

        Term parsedTerm = Term.fromDescription(term);
        Semester foundSemester = semesterRepositoryV3.getByYearAndTerm(year, parsedTerm);
        String semester = foundSemester.getSemester();

        // 이름이 같은 강의는 중복되길래 빼버렸음
        Map<String, Lecture> lectureMap = lectureRepositoryV2.findAllByCodesAndSemester(codes, semester)
            .orElseThrow(() -> new NotFoundSemesterAndCourseTypeException("학기나 이수구분을 찾을 수 없습니다."))
            .stream()
            .collect(Collectors.toMap(Lecture::getName, Function.identity(), (existing, duplicate) -> existing));

        List<Lecture> lectures = new ArrayList<>(lectureMap.values());

        return CourseTypeLectureResponse.of(semester, lectures);
    }

    public List<GeneralEducationArea> getAllGeneralEducationArea() {
        return generalEducationAreaRepository.findAll();
    }

    public GeneralEducationLectureResponse getEducationLecture(Integer userId) {
        String studentYear = StudentUtil.parseStudentNumberYearAsString(
            studentRepository.getById(userId).getStudentNumber());
        if (studentYear == null) {
            throw new StudentNumberNotFoundException("학번을 추가하세요.");
        }
        List<TimetableFrame> timetableFrames = timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId);

        List<GeneralEducationLectureResponse.GeneralEducationArea> educationAreas = new ArrayList<>();
        // 교양 선택
        educationAreas.addAll(getSelectiveEducationAreas(timetableFrames));
        // 일반 교양
        educationAreas.addAll(getGeneralEducationAreas(studentYear, timetableFrames));

        return GeneralEducationLectureResponse.of(educationAreas);
    }

    private List<GeneralEducationLectureResponse.GeneralEducationArea> getSelectiveEducationAreas(
        List<TimetableFrame> timetableFrames) {

        List<TimetableLecture> selectiveEducationTimetableLectures = timetableFrames.stream()
            .flatMap(frame -> frame.getTimetableLectures().stream())
            .filter(lecture -> lecture.getGeneralEducationArea() == null
                && lecture.getCourseType() == courseTypeRepository.getByName(GENERAL_EDUCATION_COURSE_TYPE))
            .toList();

        Integer requiredCredit = SELECTIVE_EDUCATION_REQUIRED_CREDIT;
        Integer completedCredit = 0;
        List<String> lectureNames = new ArrayList<>();

        for (TimetableLecture timetableLecture : selectiveEducationTimetableLectures) {
            Lecture lecture = timetableLecture.getLecture();
            completedCredit += Integer.parseInt(lecture != null ? lecture.getGrades() : timetableLecture.getGrades());
            lectureNames.add(lecture != null ? lecture.getName() : timetableLecture.getClassTitle());
        }

        List<GeneralEducationLectureResponse.GeneralEducationArea> educationAreas = new ArrayList<>();
        educationAreas.add(
            GeneralEducationLectureResponse.GeneralEducationArea.of(
                GENERAL_EDUCATION_COURSE_TYPE, requiredCredit, completedCredit, lectureNames)
        );

        return educationAreas;
    }

    private List<GeneralEducationLectureResponse.GeneralEducationArea> getGeneralEducationAreas(
        String studentYear, List<TimetableFrame> timetableFrames) {

        List<TimetableLecture> generalEducationTimetableLectures = timetableFrames.stream()
            .flatMap(frame -> frame.getTimetableLectures().stream())
            .filter(lecture -> lecture.getGeneralEducationArea() != null)
            .toList();

        List<GeneralEducationArea> generalEducationAreas = GeneralEducationAreaEnum.fromYear(studentYear)
            .getAreasWithCredits().keySet().stream()
            .map(generalEducationAreaRepository::getGeneralEducationAreaByName)
            .toList();

        List<GeneralEducationLectureResponse.GeneralEducationArea> educationAreas = new ArrayList<>();

        for (GeneralEducationArea generalEducationArea : generalEducationAreas) {
            Integer requiredCredit = getRequiredCredits(studentYear, generalEducationArea.getName());
            Integer completedCredit = 0;
            List<String> lectureNames = new ArrayList<>();

            for (TimetableLecture timetableLecture : generalEducationTimetableLectures) {
                if (Objects.equals(timetableLecture.getGeneralEducationArea(), generalEducationArea)) {
                    Lecture lecture = timetableLecture.getLecture();
                    completedCredit += Integer.parseInt(
                        lecture != null ? lecture.getGrades() : timetableLecture.getGrades());
                    lectureNames.add(lecture != null ? lecture.getName() : timetableLecture.getClassTitle());
                }
            }

            educationAreas.add(
                GeneralEducationLectureResponse.GeneralEducationArea.of(
                    generalEducationArea.getName(), requiredCredit, completedCredit, lectureNames)
            );
        }

        return educationAreas;
    }

    private Integer getRequiredCredits(String year, String areaName) {
        GeneralEducationAreaEnum generalEducationAreaEnum = GeneralEducationAreaEnum.fromYear(year);
        return generalEducationAreaEnum.getAreasWithCredits().get(areaName);
    }
}
