package in.koreatech.koin._common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SingleMenuPriceValidator implements ConstraintValidator<SingleMenuPrice, Object> {

    @Override
    public void initialize(SingleMenuPrice constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        boolean isSingle = false;
        Object optionPrices = null;
        Object singlePrice = null;

        try {
            isSingle = (boolean) value.getClass().getMethod("isSingle").invoke(value);
            optionPrices = value.getClass().getMethod("optionPrices").invoke(value);
            singlePrice = value.getClass().getMethod("singlePrice").invoke(value);
        } catch (Exception e) {
            context.buildConstraintViolationWithTemplate("객체가 필요한 메서드를 구현하지 않았습니다.")
                .addConstraintViolation();
            return false;
        }

        if (isSingle) {
            if (optionPrices != null) {
                context.buildConstraintViolationWithTemplate("단일 메뉴일 때 옵션 가격 리스트는 null이어야 합니다.")
                    .addPropertyNode("optionPrices").addConstraintViolation();
                return false;
            }
            if (singlePrice == null) {
                context.buildConstraintViolationWithTemplate("단일 메뉴일 때 단일 메뉴 가격은 필수입니다.")
                    .addPropertyNode("singlePrice").addConstraintViolation();
                return false;
            }
        } else {
            if (optionPrices == null || (optionPrices instanceof java.util.List && ((java.util.List<?>) optionPrices).isEmpty())) {
                context.buildConstraintViolationWithTemplate("단일 메뉴가 아닐 때 옵션 가격 리스트는 필수입니다.")
                    .addPropertyNode("optionPrices").addConstraintViolation();
                return false;
            }
            if (singlePrice != null) {
                context.buildConstraintViolationWithTemplate("단일 메뉴가 아닐 때 단일 메뉴 가격은 null이어야 합니다.")
                    .addPropertyNode("singlePrice").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
