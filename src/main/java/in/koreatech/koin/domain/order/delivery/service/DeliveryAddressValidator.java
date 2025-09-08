package in.koreatech.koin.domain.order.delivery.service;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.domain.order.delivery.model.OffCampusDeliveryAddress;

@Component
public class DeliveryAddressValidator {

    private static final String ALLOWED_SI_DO = "충청남도";
    private static final String ALLOWED_SI_GUN_GU = "천안시 동남구";
    private static final String ALLOWED_EUP_MYEON_DONG = "병천면";
    private static final String NOT_ALLOWED_BUILDING_NAME = "한국기술교육대학교";

    /*
     * 추후 각 상점 별 배달 가능 지역 요구 사항이 추가 되면 검증 로직을 추가 해야 함
     * */
    public void validateOffCampusAddress(OffCampusDeliveryAddress address) {
        if (!address.isValidDeliveryArea(ALLOWED_SI_DO, ALLOWED_SI_GUN_GU, ALLOWED_EUP_MYEON_DONG)) {
            throw CustomException.of(ApiResponseCode.INVALID_DELIVERY_AREA);
        }
        if (address.isNotAllowedBuilding(NOT_ALLOWED_BUILDING_NAME)) {
            throw CustomException.of(ApiResponseCode.INVALID_DELIVERY_BUILDING);
        }
    }
}
