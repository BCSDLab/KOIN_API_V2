package in.koreatech.koin.domain.graduation.aop;

import in.koreatech.koin.domain.graduation.repository.DetectGraduationCalculationRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class GraduationAspect {

    private final DetectGraduationCalculationRepository detectGraduationCalculationRepository;

    /**
     * 졸업 요건 계산 변경 여부를 업데이트할 controller 메서드.
     */
    @AfterReturning(pointcut = "execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.createTimetableLecture(..)) || " +
            "execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.updateTimetableLecture(..)) || " +
            "execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.deleteTimetableLecture(..)) || " +
            "execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.createTimetablesFrame(..)) || " +
            "execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.updateTimetableFrame(..)) || " +
            "execution(* in.koreatech.koin.domain.timetableV2.controller.TimetableControllerV2.deleteTimetablesFrame(..))",
            returning = "result")
    public void afterTimetableLecture(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        Integer userId = (Integer) args[1];

        detectGraduationCalculationRepository.findByUserId(userId).ifPresent(detectGraduationCalculation -> {
            detectGraduationCalculation.updatedIsChanged(true);
        });
    }
}
