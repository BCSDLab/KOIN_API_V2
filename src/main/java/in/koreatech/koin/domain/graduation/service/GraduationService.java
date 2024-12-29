package in.koreatech.koin.domain.graduation.service;

import static in.koreatech.koin.domain.student.util.StudentUtil.parseStudentNumberYear;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GraduationService {

    private final StudentRepository studentRepository;
    private final StudentCourseCalculationRepository studentCourseCalculationRepository;
    private final StandardGraduationRequirementsRepository standardGraduationRequirementsRepository;
    private final DetectGraduationCalculationRepository detectGraduationCalculationRepository;

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
