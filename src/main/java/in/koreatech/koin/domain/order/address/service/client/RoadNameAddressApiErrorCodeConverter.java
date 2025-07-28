package in.koreatech.koin.domain.order.address.service.client;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.code.ApiResponseCode;

@Component
public class RoadNameAddressApiErrorCodeConverter {

    public ApiResponseCode convertToKoinErrorCode(String externalApiErrorCode) {
        return switch (externalApiErrorCode) {
            case "E0005" -> ApiResponseCode.ADDRESS_KEYWORD_NOT_PROVIDED;
            case "E0006" -> ApiResponseCode.ADDRESS_KEYWORD_TOO_EXTENSIVE;
            case "E0008" -> ApiResponseCode.ADDRESS_KEYWORD_TOO_SHORT;
            case "E0009" -> ApiResponseCode.ADDRESS_KEYWORD_ONLY_NUMBER;
            case "E0010" -> ApiResponseCode.ADDRESS_KEYWORD_TOO_LONG;
            case "E0013" -> ApiResponseCode.ADDRESS_KEYWORD_INVALID_SYMBOLS;
            case "E0015" -> ApiResponseCode.ADDRESS_SEARCH_LIMIT_EXCEEDED;
            default -> ApiResponseCode.EXTERNAL_API_ERROR;
        };
    }
}
