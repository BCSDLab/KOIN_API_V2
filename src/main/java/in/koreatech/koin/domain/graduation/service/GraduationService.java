package in.koreatech.koin.domain.graduation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;
import in.koreatech.koin.domain.graduation.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.graduation.repository.StudentCourseCalculationRepository;
import in.koreatech.koin.domain.student.exception.DepartmentNotFoundException;
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
}
