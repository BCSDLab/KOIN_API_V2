package in.koreatech.koin.admin.shop.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.shop.dto.shop.AdminShopOrderServiceRequestsResponse;
import in.koreatech.koin.admin.shop.repository.shop.AdminShopOrderServiceRequestRepository;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminShopOrderServiceRequestService {

    private final AdminShopOrderServiceRequestRepository requestRepository;

    public AdminShopOrderServiceRequestsResponse getRequests(Integer page, Integer limit,
        ShopOrderServiceRequestStatus status, String keyword) {
        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageable = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return AdminShopOrderServiceRequestsResponse.from(requestRepository.findRequests(status, keyword, pageable));
    }
}
