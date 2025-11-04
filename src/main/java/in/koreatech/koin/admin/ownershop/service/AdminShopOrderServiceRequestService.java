package in.koreatech.koin.admin.ownershop.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.ownershop.dto.AdminShopOrderServiceResponse;
import in.koreatech.koin.admin.ownershop.dto.ShopOrderServiceRequestCondition;
import in.koreatech.koin.admin.ownershop.repository.AdminShopOrderServiceRequestRepository;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminShopOrderServiceRequestService {

    private final AdminShopOrderServiceRequestRepository adminShopOrderServiceRequestRepository;

    public AdminShopOrderServiceResponse getOrderServiceRequests(ShopOrderServiceRequestCondition condition) {
        condition.checkDataConstraintViolation();

        Long totalRequests = getTotalRequestsCount(condition);
        Criteria criteria = Criteria.of(condition.page(), condition.limit(), totalRequests.intValue());
        Sort.Direction direction = condition.getDirection();

        Page<ShopOrderServiceRequest> result = getOrderServiceRequestsResultPage(condition, criteria, direction);

        return AdminShopOrderServiceResponse.of(result, criteria);
    }

    private Long getTotalRequestsCount(ShopOrderServiceRequestCondition condition) {
        if (condition.searchType() == ShopOrderServiceRequestCondition.SearchType.SHOP_NAME
            && condition.query() != null) {
            return adminShopOrderServiceRequestRepository.countByShopName(condition.query());
        }
        if (condition.searchType() == ShopOrderServiceRequestCondition.SearchType.STATUS && condition.query() != null) {
            ShopOrderServiceRequestStatus status = ShopOrderServiceRequestStatus.valueOf(
                condition.query().toUpperCase());
            return adminShopOrderServiceRequestRepository.countByStatus(status);
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

        if (condition.searchType() == ShopOrderServiceRequestCondition.SearchType.SHOP_NAME
            && condition.query() != null) {
            return adminShopOrderServiceRequestRepository.findPageOrderServiceRequestsByShopName(
                condition.query(),
                pageRequest
            );
        }
        if (condition.searchType() == ShopOrderServiceRequestCondition.SearchType.STATUS
            && condition.query() != null) {
            ShopOrderServiceRequestStatus status = ShopOrderServiceRequestStatus.valueOf(
                condition.query().toUpperCase());
            return adminShopOrderServiceRequestRepository.findPageOrderServiceRequestsByStatus(
                status,
                pageRequest
            );
        }
        return adminShopOrderServiceRequestRepository.findPageOrderServiceRequests(pageRequest);
    }
}
