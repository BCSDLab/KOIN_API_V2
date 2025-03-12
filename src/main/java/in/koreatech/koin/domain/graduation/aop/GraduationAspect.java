package in.koreatech.koin.domain.graduation.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.graduation.repository.DetectGraduationCalculationRepository;
import in.koreatech.koin.global.auth.AuthContext;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class GraduationAspect {

    private final AuthContext authContext;
    private final DetectGraduationCalculationRepository detectGraduationCalculationRepository;

    /**
     * 졸업 요건 계산 변경 여부를 업데이트할 controller 메서드.
     */
    @AfterReturning(
        pointcut = """
        execution(* in.koreatech.koin.domain.timetable.controller.TimetableController.createTimetables(..)) ||
        execution(* in.koreatech.koin.domain.timetable.controller.TimetableController.updateTimetable(..)) ||
        execution(* in.koreatech.koin.domain.timetable.controller.TimetableController.deleteTimetableById(..)) ||
        execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.createTimetableLecture(..)) ||
        execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.updateTimetableLecture(..)) ||
        execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.deleteTimetableLecture(..)) ||
        execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.deleteTimetableLectures(..)) ||
        execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.deleteTimetableLectureByFrameId(..)) ||
        execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.deleteAllTimetablesFrame(..)) ||
        execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.deleteTimetablesFrame(..)) ||
        execution(* in.koreatech.koin.domain.timetableV3.controller.TimetableRegularLectureControllerV3.createTimetablesRegularLecture(..)) ||
        execution(* in.koreatech.koin.domain.timetableV3.controller.TimetableRegularLectureControllerV3.updateTimetablesRegularLecture(..)) ||
        execution(* in.koreatech.koin.domain.graduation.controller.GraduationController.uploadStudentGradeExcelFile(..)) ||
        execution(* in.koreatech.koin.domain.timetableV3.controller.TimetableFrameControllerV3.deleteTimetablesFrames(..))
        """,
        returning = "result")
    public void afterTimetableLecture(JoinPoint joinPoint, Object result) {
        Integer userId = authContext.getUserId();

        detectGraduationCalculationRepository.findByUserId(userId).ifPresent(detectGraduationCalculation -> {
            detectGraduationCalculation.updatedIsChanged(true);
            detectGraduationCalculationRepository.save(detectGraduationCalculation);
        });
    }
}
