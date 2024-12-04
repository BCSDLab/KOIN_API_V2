package in.koreatech.koin.domain.timetableV2.dto.validation;

import java.util.List;

import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest.InnerTimetableLectureRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InnerTimetableLectureRequestValidator implements ConstraintValidator<InnerTimetableLectureRequestValid, List<InnerTimetableLectureRequest>> {

    @Override
    public void initialize(InnerTimetableLectureRequestValid constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<InnerTimetableLectureRequest> elements, ConstraintValidatorContext context) {
        return elements == null || elements.isEmpty();
    }
}