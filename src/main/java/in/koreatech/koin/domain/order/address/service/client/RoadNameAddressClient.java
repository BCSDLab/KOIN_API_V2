package in.koreatech.koin.domain.order.address.service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import in.koreatech.koin.domain.order.address.dto.RoadNameAddressApiResponse;
import in.koreatech.koin.domain.order.address.exception.AddressApiException;
import in.koreatech.koin.domain.order.address.exception.AddressErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoadNameAddressClient {

    private final RestTemplate restTemplate;

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
            RoadNameAddressApiResponse apiResponse = restTemplate.postForObject(apiUrl, requestEntity,
                RoadNameAddressApiResponse.class);
            if (apiResponse == null || apiResponse.results() == null || apiResponse.results().common() == null) {
                throw new AddressApiException(AddressErrorCode.EXTERNAL_API_ERROR);
            }

            String errorCode = apiResponse.results().common().errorCode();
            if (!errorCode.equals(SUCCESS_CODE)) {
                AddressErrorCode addressErrorCode = AddressErrorCode.from(errorCode);
                String originalErrorMessage = apiResponse.results().common().errorMessage();
                throw new AddressApiException(addressErrorCode, originalErrorMessage);
            }
            return apiResponse;

        } catch (RestClientException e) {
            log.error("주소 API 호출 실패", e);
            throw new AddressApiException(AddressErrorCode.EXTERNAL_API_ERROR, e.getMessage());
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
}
