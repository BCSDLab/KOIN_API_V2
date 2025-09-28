package in.koreatech.koin.domain.order.address.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.address.client.AddressClient;
import in.koreatech.koin.domain.order.address.dto.AddressSearchRequest;
import in.koreatech.koin.domain.order.address.dto.AddressSearchResponse;
import in.koreatech.koin.domain.order.address.dto.RoadNameAddressApiResponse;
import in.koreatech.koin.domain.order.address.model.RoadNameAddressDocument;
import in.koreatech.koin.domain.order.address.repository.RoadNameAddressRepository;
import in.koreatech.koin.global.exception.CustomException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressSearchOpenApiService {

    private final AddressClient roadNameAddressClient;
    private final RoadNameAddressRepository addressRepository;
    private static final String ROAD_NAME_ADDRESS = "roadNameAddress";

    @CircuitBreaker(name = ROAD_NAME_ADDRESS, fallbackMethod = "searchAddressFallback")
    public AddressSearchResponse searchAddress(AddressSearchRequest request) {
        RoadNameAddressApiResponse apiResponse = roadNameAddressClient.searchAddress(
            request.keyword(),
            request.currentPage(),
            request.countPerPage()
        );

        return AddressSearchResponse.from(apiResponse);
    }

    /**
     * searchAddress 에서 예외 반환 시 자동 호출 (CircuitBreaker OPEN 여부와 관련 없음)<br>
     * 예외가 400인 경우 => 클라이언트에 그대로 예외 반환<br>
     * 예외가 500인 경우 => 외부 API 호출 장애. 로컬 DB에서 결과 반환
     * */
    private AddressSearchResponse searchAddressFallback(AddressSearchRequest request, CustomException e) {
        if (e.getErrorCode().getHttpStatus().is5xxServerError()) {
            log.warn("[Address API-5xx] 로컬 DB 에서 결과를 반환합니다. {}", e.getMessage());
            return searchAddressFromLocal(request);
        }
        throw CustomException.of(e.getErrorCode(), e.getMessage());
    }

    /**
     * CircuitBreaker OPEN인 경우 searchAddress 호출 없이 바로 호출<br>
     * 로컬 DB에서 결과 반환
     * */
    private AddressSearchResponse searchAddressFallback(AddressSearchRequest request, CallNotPermittedException e) {
        log.error("[Address API CircuitBreaker OPEN] 로컬 DB 에서 결과를 반환합니다.");
        return searchAddressFromLocal(request);
    }

    public AddressSearchResponse searchAddressFromLocal(AddressSearchRequest request) {
        Pageable pageable = PageRequest.of(request.currentPage() - 1, request.countPerPage());
        Page<RoadNameAddressDocument> resultPage = addressRepository.findByKeyword(request.keyword(), pageable);
        return AddressSearchResponse.from(resultPage);
    }
}
