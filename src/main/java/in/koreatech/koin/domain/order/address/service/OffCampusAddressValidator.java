package in.koreatech.koin.domain.order.address.service;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.order.address.exception.AddressErrorCode;
import in.koreatech.koin.domain.order.address.exception.AddressException;
import in.koreatech.koin.domain.order.address.model.OffCampusDeliveryAddress;

@Component
public class OffCampusAddressValidator {

    private static final String ALLOWED_SI_DO = "충청남도";
    private static final String ALLOWED_SI_GUN_GU = "천안시 동남구";
    private static final String ALLOWED_EUP_MYEON_DONG = "병천면";

    /*
    * 추후 각 상점 별 배달 가능 지역 요구 사항이 추가 되면 검증 로직을 추가 해야 함
    * */
    public void validateAddress(OffCampusDeliveryAddress address) {
        if (!address.isValidDeliveryArea(ALLOWED_SI_DO, ALLOWED_SI_GUN_GU, ALLOWED_EUP_MYEON_DONG)) {
            throw new AddressException(AddressErrorCode.INVALID_DELIVERY_AREA);
        }
    }
}
