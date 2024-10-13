package in.koreatech.koin.domain.graduation.service;

import in.koreatech.koin.domain.graduation.dto.GraduationCourseCalculationResponse;
import in.koreatech.koin.domain.graduation.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.Department;
import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.graduation.repository.DepartmentRepository;
import in.koreatech.koin.domain.graduation.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.graduation.repository.StudentCourseCalculationRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraduationService {

    private final StudentRepository studentRepository;
    private final StudentCourseCalculationRepository studentCourseCalculationRepository;
    private final StandardGraduationRequirementsRepository standardGraduationRequirementsRepository;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final CatalogRepository catalogRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public void createStudentCourseCalculation(Integer userId) {
        Student student = studentRepository.getById(userId);

        if (student.getDepartment() == null) {
            DepartmentNotFoundException.withDetail("학과를 추가하세요.");
        }
        if (student.getStudentNumber() == null) {
            DepartmentNotFoundException.withDetail("학번을 추가하세요.");
        }

        List<StandardGraduationRequirements> StandardGraduationRequirementsList = standardGraduationRequirementsRepository.
                findAllByDepartmentAndYear(student.getDepartment(), student.getStudentNumber().substring(0, 4));
        for (StandardGraduationRequirements standardGraduationRequirements : StandardGraduationRequirementsList) {
            StudentCourseCalculation studentCourseCalculation = StudentCourseCalculation.builder()
                    .completedGrades(0)
                    .user(student.getUser())
                    .standardGraduationRequirements(standardGraduationRequirements)
                    .build();
            studentCourseCalculationRepository.save(studentCourseCalculation);
        }

    }

    public GraduationCourseCalculationResponse getGraduationCourseCalculationResponse(Integer userId) {
        Student student = studentRepository.getById(userId);
        Department department = departmentRepository.findByName(student.getDepartment().getName()).get();

        if (student.getDepartment() == null) {
            DepartmentNotFoundException.withDetail("학과를 추가하세요.");
        }
        if (student.getStudentNumber() == null) {
            DepartmentNotFoundException.withDetail("학번을 추가하세요.");
        }

        String studentYear = student.getStudentNumber().substring(0, 4);

        List<TimetableLecture> timetableLectures = timetableFrameRepositoryV2.findAllByUserId(userId).stream()
            .flatMap(frame -> frame.getTimetableLectures().stream()).toList();

        List<Catalog> catalogList = new ArrayList<>();
        timetableLectures.stream()
            .map(timetableLecture -> timetableLecture.getLecture().getName())
            .forEach(lectureName -> {
                List<Catalog> catalogs = catalogRepository.findByLectureNameAndYearAndDepartment(
                    lectureName, studentYear, student.getDepartment());
                catalogList.addAll(catalogs);
            });

        Map<Integer, Integer> courseTypeCreditsMap = catalogList.stream()
            .collect(Collectors.toMap(
                catalog -> catalog.getCourseType().getId(),
                Catalog::getCredit,
                Integer::sum));

        List<StandardGraduationRequirements> graduationRequirements = catalogList.stream()
            .map(catalog -> standardGraduationRequirementsRepository.findByDepartmentIdAndCourseTypeIdAndYear(
                catalog.getDepartment().getId(),
                catalog.getCourseType().getId(),
                studentYear
            ))
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();

        for (StandardGraduationRequirements requirement : graduationRequirements) {
            StudentCourseCalculation existingCalculation = studentCourseCalculationRepository.findByUserIdAndStandardGraduationRequirementsId(
                userId, requirement.getId());

            if (existingCalculation == null) {
                int completedGrades = courseTypeCreditsMap.getOrDefault(requirement.getCourseType().getId(), 0);

                StudentCourseCalculation newCalculation = StudentCourseCalculation.builder()
                    .completedGrades(completedGrades)
                    .user(student.getUser())
                    .standardGraduationRequirements(requirement)
                    .build();

                studentCourseCalculationRepository.save(newCalculation);
            }
        }
    }
}
