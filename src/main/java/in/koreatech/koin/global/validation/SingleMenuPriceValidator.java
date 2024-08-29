package in.koreatech.koin.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import in.koreatech.koin.admin.shop.dto.AdminCreateMenuRequest;

public class SingleMenuPriceValidator implements ConstraintValidator<SingleMenuPrice, AdminCreateMenuRequest> {

    @Override
    public void initialize(SingleMenuPrice constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(AdminCreateMenuRequest request, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        if (request.isSingle()) {
            if (request.optionPrices() != null) {
                context.buildConstraintViolationWithTemplate("단일 메뉴일 때 옵션 가격 리스트는 null이어야 합니다.")
                    .addPropertyNode("optionPrices").addConstraintViolation();
                isValid = false;
            }
            if (request.singlePrice() == null) {
                context.buildConstraintViolationWithTemplate("단일 메뉴일 때 단일 메뉴 가격은 필수입니다.")
                    .addPropertyNode("singlePrice").addConstraintViolation();
                isValid = false;
            }
        } else {
            if (request.optionPrices() == null || request.optionPrices().isEmpty()) {
                context.buildConstraintViolationWithTemplate("단일 메뉴가 아닐 때 옵션 가격 리스트는 필수입니다.")
                    .addPropertyNode("optionPrices").addConstraintViolation();
                isValid = false;
            }
            if (request.singlePrice() != null) {
                context.buildConstraintViolationWithTemplate("단일 메뉴가 아닐 때 단일 메뉴 가격은 null이어야 합니다.")
                    .addPropertyNode("singlePrice").addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
