package in.koreatech.koin.domain.collegecredit.aop;

import in.koreatech.koin.domain.collegecredit.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.collegecredit.model.StudentCourseCalculation;
import in.koreatech.koin.domain.collegecredit.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.collegecredit.repository.StudentCourseCalculationRepository;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class CreditCalculationAspect {

    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final StudentRepository studentRepository;
    private final StudentCourseCalculationRepository studentCourseCalculationRepository;
    private final StandardGraduationRequirementsRepository standardGraduationRequirementsRepository;

    @AfterReturning(pointcut = "execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.updateTimetableLecture(..)) " +
            "|| execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.deleteTimetableLecture(..))")
    public void afterReturning(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Integer userId = (Integer) args[1];
        calculateCredit(userId);
    }

    private void calculateCredit(Integer userId) {
        Student student = studentRepository.getById(userId);
        List<StandardGraduationRequirements> standardGraduationRequirements =
                standardGraduationRequirementsRepository.findByYearAndDepartment(student.getStudentNumber().substring(0, 4),
                        student.getDepartment());
        for (StandardGraduationRequirements standardGraduationRequirement : standardGraduationRequirements) {
            Integer standardGraduationRequirementId = standardGraduationRequirement.getId();
            List<TimetableFrame> timetableFrames = timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(student.getUser().getId());
            for (TimetableFrame timetableFrame : timetableFrames) {
                List<TimetableLecture> timetableLectures = timetableLectureRepositoryV2
                        .findAllByStandardGraduationRequirementsIdAndTimetableFrameId(standardGraduationRequirementId, timetableFrame.getId());
                int grades = timetableLectures.stream()
                        .mapToInt(timeTableLecture -> {
                            if (timeTableLecture.getLecture() != null) {
                                return Integer.parseInt(timeTableLecture.getLecture().getGrades());
                            } else {
                                return Integer.parseInt(timeTableLecture.getGrades());
                            }
                        })
                        .sum();
                Optional<StudentCourseCalculation> studentCourseCalculation = studentCourseCalculationRepository
                        .findByUserIdAndStandardGraduationRequirementsId(userId, standardGraduationRequirementId);
                studentCourseCalculation.ifPresent(courseCalculation -> courseCalculation.calculateCompletedGrades(grades));
            }
        }
    }
}
