package in.koreatech.koin.domain.graduation.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import in.koreatech.koin.domain.graduation.dto.EducationLectureResponse;
import in.koreatech.koin.domain.graduation.dto.GraduationCourseCalculationResponse;
import in.koreatech.koin.domain.graduation.exception.ExcelFileCheckException;
import in.koreatech.koin.domain.graduation.exception.ExcelFileNotFoundException;
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
import in.koreatech.koin.global.exception.DuplicationException;
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
    private final GeneralEducationAreaRepository generalEducationAreaRepository;

    private static final String MIDDLE_TOTAL = "소 계";
    private static final String TOTAL = "합 계";
    private static final String RETAKE = "Y";
    private static final String UNSATISFACTORY = "U";
    private static final String DEFAULTCOURSERTYPE = "이수구분선택";
    private static final String GENERALEDUCATIONCOURSETYPE = "교양선택";

    @Transactional
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

    @Transactional
    public void resetStudentCourseCalculation(Student student, Major newMajor) {
        // 기존 학생 졸업요건 계산 정보 삭제
        studentCourseCalculationRepository.deleteAllByUserId(student.getUser().getId());

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
            return getExistingGraduationCalculation(userId);
        }

        studentCourseCalculationRepository.deleteAllByUserId(userId);

        Student student = getValidatedStudent(userId);
        String studentYear = StudentUtil.parseStudentNumberYearAsString(student.getStudentNumber());

        List<Catalog> catalogList = getCatalogListForStudent(student, studentYear);
        Map<Integer, Integer> courseTypeCreditsMap = calculateCourseTypeCredits(catalogList);

        List<GraduationCourseCalculationResponse.InnerCalculationResponse> courseTypes = processGraduationCalculations(
            student, courseTypeCreditsMap
        );

        detectGraduationCalculation.updatedIsChanged(false);

        return GraduationCourseCalculationResponse.of(courseTypes);
    }

    @Transactional
    public void readStudentGradeExcelFile(MultipartFile file, Integer userId) throws IOException {
        checkFiletype(file);
        Student student = studentRepository.getById(userId);
        String studentYear = StudentUtil.parseStudentNumberYearAsString(student.getStudentNumber());

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new HSSFWorkbook(inputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            TimetableFrame graduationFrame = null;
            String currentSemester = "default";

            Set<String> lectureNames = new HashSet<>();
            Set<String> lectureCodes = new HashSet<>();
            Set<String> semesters = new HashSet<>();
            Set<String> years = new HashSet<>();
            List<TimetableLecture> timetableLectures = new ArrayList<>();

            for (Row row : sheet) {
                GradeExcelData data = extractExcelData(row);
                if (row.getRowNum() == 0 || skipRow(data)) {
                    continue;
                }
                if (data.classTitle().equals(TOTAL)) {
                    break;
                }
                lectureNames.add(data.classTitle());
                lectureCodes.add(data.code());
                semesters.add(getKoinSemester(data.semester(), data.year()));
                years.add(data.year());
            }

            Map<String, Lecture> lectureMap = loadLectures(semesters, lectureCodes);
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
                Lecture lecture = lectureMap.get(semester + "_" + data.code());
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

    // _를 넣은 이유는, 각각의 고유한 key 값을 갖게 하기 위해서(문제가 자주 생기길래..)
    private Map<String, Lecture> loadLectures(Set<String> semesters, Set<String> lectureCodes) {
        return lectureRepositoryV2.findAllBySemesterInAndCodeIn(semesters, lectureCodes)
            .stream().collect(Collectors.toMap(l -> l.getSemester() + "_" + l.getCode(), Function.identity(),
                (existing, duplicate) -> duplicate));
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

    public CatalogResult findCourseType(Lecture lecture, GradeExcelData data,
        String studentYear, Map<String, Catalog> catalogByNameMap, Map<String, Catalog> catalogByCodeMap) {

        // 자유선택은 학생이 직접 신청하는 부분이라 매핑해줬음
        if (data.courseType().equals("자선")) {
            return new CatalogResult(courseTypeRepository.getByName(DEFAULTCOURSERTYPE), null);
        }

        if (lecture == null) {
            return new CatalogResult(courseTypeRepository.getByName(DEFAULTCOURSERTYPE), null);
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
            return new CatalogResult(courseTypeRepository.getByName(DEFAULTCOURSERTYPE), null);
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
            .classTime(lecture != null ? lecture.getClassTime() : null)
            .professor(data.professor())
            .grades(data.credit())
            .isDeleted(false)
            .lecture(lecture)
            .timetableFrame(graduationFrame)
            .courseType(courseType)
            .generalEducationArea(generalEducationArea)
            .build();
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

    private GraduationCourseCalculationResponse getExistingGraduationCalculation(Integer userId) {
        List<StudentCourseCalculation> existingCalculations = studentCourseCalculationRepository.findAllByUserId(userId);

        List<GraduationCourseCalculationResponse.InnerCalculationResponse> courseTypes = existingCalculations.stream()
            .map(calc -> {
                StandardGraduationRequirements requirement = calc.getStandardGraduationRequirements();
                return GraduationCourseCalculationResponse.InnerCalculationResponse.of(
                    requirement.getCourseType().getName(),
                    requirement.getRequiredGrades(),
                    calc.getCompletedGrades()
                );
            })
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
                Catalog bestCatalog = findBestMatchingCatalog(lectureName, studentYear, student.getMajor());

                if (bestCatalog != null) {
                    catalogList.add(bestCatalog);
                }
            }
        }
        return catalogList;
    }

    private Catalog findBestMatchingCatalog(String lectureName, String studentYear, Major major) {
        // 학생의 학번 연도와 전공이 모두 일치하는 Catalog 조회
        List<Catalog> catalogs = catalogRepository.findByLectureNameAndYearAndMajor(lectureName, studentYear, major);
        if (!catalogs.isEmpty()) {
            return catalogs.get(0);
        }

        // 학생의 학번 연도만 일치하는 Catalog 조회 (전공 무관)
        catalogs = catalogRepository.findByLectureNameAndYear(lectureName, studentYear);
        if (!catalogs.isEmpty()) {
            return catalogs.get(0);
        }

        // 강의명이 일치하는 모든 Catalog 조회
        catalogs = catalogRepository.findByLectureNameOrderByYearDesc(lectureName);
        if (!catalogs.isEmpty()) {
            return catalogs.get(0);
        }

        return null;
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

    private List<StandardGraduationRequirements> getGraduationRequirements(List<Catalog> catalogList, String studentYear) {
        return catalogList.stream()
            .map(catalog -> {
                if (catalog.getMajor() == null) {
                    return standardGraduationRequirementsRepository.findByCourseTypeIdAndYear(
                        catalog.getCourseType().getId(),
                        studentYear
                    );
                }
                return standardGraduationRequirementsRepository.findByMajorIdAndCourseTypeIdAndYear(
                    catalog.getMajor().getId(),
                    catalog.getCourseType().getId(),
                    studentYear
                );
            })
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();
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

        StudentCourseCalculation newCalculation = StudentCourseCalculation.builder()
            .completedGrades(completedGrades)
            .user(student.getUser())
            .standardGraduationRequirements(standardGraduationRequirementOpt.get())
            .build();
        studentCourseCalculationRepository.save(newCalculation);
    }

    public CourseTypeLectureResponse getLectureByCourseType(Integer year, String term, String courseTypeName,
        String generalEducationAreaName) {
        CourseType courseType = courseTypeRepository.getByName(courseTypeName);

        List<Catalog> catalogs = catalogRepository.getAllByCourseTypeId(courseType.getId());

        if (generalEducationAreaName != null) {
            GeneralEducationArea generalEducationArea =
                generalEducationAreaRepository.getGeneralEducationAreaByName(generalEducationAreaName);

            catalogs = catalogs.stream()
                .filter(catalog -> catalog.getGeneralEducationArea() != null
                    && catalog.getGeneralEducationArea().getId().equals(generalEducationArea.getId()))
                .toList();
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

    public EducationLectureResponse getEducationLecture(Integer userId) {
        List<TimetableFrame> timetableFrames = timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId);

        List<TimetableLecture> educationTimetableLectures = timetableFrames.stream()
            .flatMap(frame -> frame.getTimetableLectures().stream())
            .filter(lecture -> lecture.getGeneralEducationArea() != null)
            .collect(Collectors.groupingBy(TimetableLecture::getGeneralEducationArea))
            .values()
            .stream()
            .map(list -> list.get(0))
            .toList();

        List<GeneralEducationArea> generalEducationAreas = catalogRepository.findAllByYearAndCourseTypeId(
                StudentUtil.parseStudentNumberYearAsString(studentRepository.getById(userId).getStudentNumber()),
                courseTypeRepository.getByName(GENERALEDUCATIONCOURSETYPE).getId())
            .stream()
            .filter(catalog -> catalog.getGeneralEducationArea() != null)
            .collect(Collectors.groupingBy(Catalog::getGeneralEducationArea))
            .values()
            .stream()
            .map(list -> list.get(0).getGeneralEducationArea())
            .toList();

        Map<String, EducationLectureResponse.RequiredEducationArea> requiredEducationAreaMap = new HashMap<>();

        for (GeneralEducationArea generalEducationArea : generalEducationAreas) {
            boolean isCompleted = false;
            String lectureName = null;

            for (TimetableLecture timetableLecture : educationTimetableLectures) {
                if (timetableLecture.getGeneralEducationArea().equals(generalEducationArea)) {
                    isCompleted = true;
                    lectureName = timetableLecture.getLecture().getName();
                    break;
                }
            }

            requiredEducationAreaMap.put(generalEducationArea.getName(),
                EducationLectureResponse.RequiredEducationArea.of(generalEducationArea.getName(), isCompleted,
                    lectureName));
        }

        return EducationLectureResponse.of(new ArrayList<>(requiredEducationAreaMap.values()));
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
}
