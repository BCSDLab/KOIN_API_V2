package in.koreatech.koin.domain.address.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.address.client.AddressClient;
import in.koreatech.koin.domain.order.address.dto.RoadNameAddressApiResponse;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoadNameAddressValidator implements AddressValidator {

    private final AddressClient roadNameAddressClient;

    /**
     * 외부 주소 검색 API를 호출하여 주소의 유효성을 검증한다. 외부 API 오류 발생시 예외를 반환하지 않고 검증을 통과한 것으로 처리한다.
     * @param address 주소(상세 주소를 제외한 기본 주소. 예: 충청남도 천안시 동남구 병천면 충절로 1600)
     * @throws CustomException 주소 검색 결과가 0개 이하일 경우 유효하지 않은 주소로 판단한다.
     */
    public void validateAddress(String address) {
        try {
            RoadNameAddressApiResponse roadNameAddressApiResponse = roadNameAddressClient.searchAddress(address, 1, 1);
            if (Integer.parseInt(roadNameAddressApiResponse.results().common().totalCount()) == 0) {
                throw CustomException.of(ApiResponseCode.INVALID_ADDRESS_FORMAT);
            }
        } catch (CustomException e) {
            if (e.getErrorCode() == ApiResponseCode.EXTERNAL_API_ERROR) {
                log.error("[Address API-5xx] 주소 검증 과정에서 오류가 발생했습니다.");
                return;
            }
            throw CustomException.of(ApiResponseCode.INVALID_ADDRESS_FORMAT);
        }
    }
}
