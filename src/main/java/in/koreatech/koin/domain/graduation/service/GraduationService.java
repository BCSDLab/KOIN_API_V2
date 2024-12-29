package in.koreatech.koin.domain.graduation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;

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
    private final CatalogRepository catalogRepository;

    @Transactional
    public void createStudentCourseCalculation(Integer userId) {
        Student student = studentRepository.getById(userId);

        if (student.getDepartment() == null) {
            DepartmentNotFoundException.withDetail("학과를 추가하세요.");
        }
        if (student.getStudentNumber() == null) {
            DepartmentNotFoundException.withDetail("학번을 추가하세요.");
        }

        List<StandardGraduationRequirements> requirementsList =
            standardGraduationRequirementsRepository.findAllByDepartmentAndYear(
                student.getDepartment(), student.getStudentNumber().substring(0, 4));

        requirementsList.forEach(requirement -> studentCourseCalculationRepository.save(
            StudentCourseCalculation.builder()
                .completedGrades(0)
                .user(student.getUser())
                .standardGraduationRequirements(requirement)
                .build()));

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
        // 학번에 맞는 이수요건 정보 조회
        List<StandardGraduationRequirements> requirementsList =
            standardGraduationRequirementsRepository.findAllByDepartmentAndYear(
                newDepartment, student.getStudentNumber().substring(0, 4));
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
        // DetectGraduationCalculation 정보 업데이트
        detectGraduationCalculationRepository.findByUserId(student.getUser().getId())
            .ifPresent(detectGraduationCalculation -> {
                detectGraduationCalculation.updatedIsChanged(true);
            });
    }

    @Transactional
    public GraduationCourseCalculationResponse getGraduationCourseCalculationResponse(Integer userId) {
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
        List<TimetableLecture> timetableLectures = timetableFrameRepositoryV2.getAllByUserId(student.getId()).stream()
            .flatMap(frame -> frame.getTimetableLectures().stream())
            .toList();

        List<Catalog> catalogList = new ArrayList<>();
        timetableLectures.forEach(timetableLecture -> {
            String lectureName = timetableLecture.getLecture() != null
                ? timetableLecture.getLecture().getName()
                : timetableLecture.getClassTitle();

            if (lectureName != null) {
                List<Catalog> catalogs = catalogRepository.findByLectureNameAndYearAndDepartment(
                    lectureName, studentYear, student.getDepartment());
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
            .map(catalog -> standardGraduationRequirementsRepository.findByDepartmentIdAndCourseTypeIdAndYear(
                catalog.getDepartment().getId(),
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
            throw new IllegalStateException("CourseType이 null입니다.");
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
}
