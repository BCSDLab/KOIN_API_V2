package in.koreatech.koin.admin.shop.service;

import java.time.Clock;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.dto.shop.AdminShopOrderServiceRequestResponse;
import in.koreatech.koin.admin.shop.dto.shop.AdminShopOrderServiceRequestsResponse;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopOrderServiceRequestRepository;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.order.shop.service.OrderableShopPromotionService;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import in.koreatech.koin.domain.shop.cache.aop.RefreshShopsCache;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminShopOrderServiceRequestService {

    private final AdminShopOrderServiceRequestRepository requestRepository;
    private final OrderableShopPromotionService promotionService;
    private final Clock clock;

    @Transactional
    @RefreshShopsCache
    public void approve(Integer id) {
        ShopOrderServiceRequest request = requestRepository.getByIdForUpdate(id);
        request.requirePending();
        promotionService.promote(request);
        request.approve(clock);
    }

    @Transactional
    public void reject(Integer id, String reason) {
        ShopOrderServiceRequest request = requestRepository.getByIdForUpdate(id);
        request.reject(reason);
    }

    public AdminShopOrderServiceRequestResponse getRequest(Integer id) {
        return AdminShopOrderServiceRequestResponse.from(requestRepository.getById(id));
    }

    public AdminShopOrderServiceRequestsResponse getRequests(Integer page, Integer limit,
        ShopOrderServiceRequestStatus status, String keyword) {
        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageable = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return AdminShopOrderServiceRequestsResponse.from(requestRepository.findRequests(status, keyword, pageable));
    }
}
