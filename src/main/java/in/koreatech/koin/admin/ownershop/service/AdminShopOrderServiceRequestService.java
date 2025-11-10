package in.koreatech.koin.admin.ownershop.service;

import static in.koreatech.koin.global.code.ApiResponseCode.SEARCH_QUERY_ONLY_WHITESPACE;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceResponse;
import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServicesResponse;
import in.koreatech.koin.admin.ownershop.dto.ShopOrderServiceRequestCondition;
import in.koreatech.koin.admin.ownershop.repository.AdminShopOrderServiceRequestRepository;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminShopOrderServiceRequestService {

    private final AdminShopOrderServiceRequestRepository adminShopOrderServiceRequestRepository;

    public AdminShopOrderServicesResponse getOrderServiceRequests(ShopOrderServiceRequestCondition condition) {
        if (condition.isQueryBlank()) {
            throw CustomException.of(SEARCH_QUERY_ONLY_WHITESPACE);
        }

        Long totalRequests = getTotalRequestsCount(condition);
        Criteria criteria = Criteria.of(condition.page(), condition.limit(), totalRequests.intValue());
        Sort.Direction direction = condition.getDirection();

        Page<ShopOrderServiceRequest> result = getOrderServiceRequestsResultPage(condition, criteria, direction);

        return AdminShopOrderServicesResponse.of(result, criteria);
    }

    public AdminShopOrderServiceResponse getOrderServiceRequest(Integer id) {
        ShopOrderServiceRequest shopOrderServiceRequest = adminShopOrderServiceRequestRepository.getById(id);
        return AdminShopOrderServiceResponse.from(shopOrderServiceRequest);
    }

    private Long getTotalRequestsCount(ShopOrderServiceRequestCondition condition) {
        if (condition.isQueryNotNull() && condition.isStatusNotNull()) {
            return adminShopOrderServiceRequestRepository.countByStatusAndShopName(
                condition.status(),
                condition.query()
            );
        }

        if (condition.isQueryNotNull()) {
            return adminShopOrderServiceRequestRepository.countByShopName(condition.query());
        }

        if (condition.isStatusNotNull()) {
            return adminShopOrderServiceRequestRepository.countByStatus(condition.status());
        }

        return adminShopOrderServiceRequestRepository.count();
    }

    private Page<ShopOrderServiceRequest> getOrderServiceRequestsResultPage(
        ShopOrderServiceRequestCondition condition,
        Criteria criteria,
        Sort.Direction direction
    ) {
        PageRequest pageRequest = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(direction, "createdAt")
        );

        if (condition.isQueryNotNull() && condition.isStatusNotNull()) {
            return adminShopOrderServiceRequestRepository.findPageOrderServiceRequestsByStatusAndShopName(
                condition.status(),
                condition.query(),
                pageRequest
            );
        }

        if (condition.isQueryNotNull()) {
            return adminShopOrderServiceRequestRepository.findPageOrderServiceRequestsByShopName(
                condition.query(),
                pageRequest
            );
        }

        if (condition.isStatusNotNull()) {
            return adminShopOrderServiceRequestRepository.findPageOrderServiceRequestsByStatus(
                condition.status(),
                pageRequest
            );
        }

        return adminShopOrderServiceRequestRepository.findPageOrderServiceRequests(pageRequest);
    }
}
