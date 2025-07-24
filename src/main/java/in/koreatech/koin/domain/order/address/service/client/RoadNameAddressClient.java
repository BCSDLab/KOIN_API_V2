package in.koreatech.koin.domain.order.address.service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.order.address.dto.RoadNameAddressApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
public class RoadNameAddressClient {

    private final RestTemplate restTemplate;
    private final RoadNameAddressApiErrorCodeConverter errorCodeConverter;

    @Value("${address.api.key}")
    private String apiKey;

    @Value("${address.api.url}")
    private String apiUrl;
    private static final String SUCCESS_CODE = "0";

    public RoadNameAddressApiResponse searchAddress(String keyword, int currentPage, int countPerPage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var requestEntity = getHttpRequestEntity(headers, keyword, currentPage, countPerPage);
        try {
            var apiResponse = restTemplate.postForObject(apiUrl, requestEntity, RoadNameAddressApiResponse.class);

            validateApiResponse(apiResponse);

            handleExternalApiException(
                apiResponse.results().common().errorCode(),
                apiResponse.results().common().errorMessage()
            );

            return apiResponse;
        } catch (RestClientException e) {
            throw CustomException.of(ApiResponseCode.EXTERNAL_API_ERROR, "주소 API 호출 실패");
        }
    }

    private HttpEntity<MultiValueMap<String, String>> getHttpRequestEntity(HttpHeaders headers, String keyword,
        int currentPage, int countPerPage) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("confmKey", apiKey);
        body.add("currentPage", String.valueOf(currentPage));
        body.add("countPerPage", String.valueOf(countPerPage));
        body.add("keyword", keyword);
        body.add("resultType", "json");
        return new HttpEntity<>(body, headers);
    }

    private void validateApiResponse(RoadNameAddressApiResponse apiResponse) {
        if (apiResponse == null || apiResponse.results() == null || apiResponse.results().common() == null) {
            throw CustomException.of(ApiResponseCode.EXTERNAL_API_ERROR, "주소 API 호출 실패");
        }
    }

    private void handleExternalApiException(String apiErrorCode, String apiErrorMessage) {
        if (!apiErrorCode.equals(SUCCESS_CODE)) {
            ApiResponseCode apiResponseCode = errorCodeConverter.convertToKoinErrorCode(apiErrorCode);
            throw CustomException.of(apiResponseCode, apiErrorMessage);
        }
    }
}
