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

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoadNameAddressClient {

    private final RestTemplate restTemplate;

    @Value("${ROAD_NAME_ADDRESS_OPEN_API_KEY}")
    private String apiKey;
    private static final String OPEN_API_URL = "https://business.juso.go.kr/addrlink/addrLinkApi.do";

    public RoadNameAddressApiResponse searchAddress(String keyword, int currentPage, int countPerPage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var requestEntity = getHttpRequestEntity(headers, keyword, currentPage, countPerPage);

        try {
            RoadNameAddressApiResponse apiResponse = restTemplate.postForObject(OPEN_API_URL, requestEntity,
                RoadNameAddressApiResponse.class);

            if (apiResponse == null || apiResponse.results() == null || apiResponse.results().common() == null) {
                throw new AddressApiException(AddressErrorCode.EXTERNAL_API_ERROR);
            }

            String errorCode = apiResponse.results().common().errorCode();
            if (!errorCode.equals("0")) {
                AddressErrorCode addressErrorCode = AddressErrorCode.from(errorCode);
                String originalErrorMessage = apiResponse.results().common().errorMessage();
                throw new AddressApiException(addressErrorCode, originalErrorMessage);
            }
            return apiResponse;

        } catch (RestClientException e) {
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
