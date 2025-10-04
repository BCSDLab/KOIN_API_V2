package in.koreatech.koin.domain.shoptoOrderable.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.domain.shoptoOrderable.dto.ShopToOrderableRequest;
import in.koreatech.koin.domain.shoptoOrderable.repository.ShopToOrderableRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopToOrderableService {
    private final ShopToOrderableRepository shopToOrderableRepository;
    public void createOrderableRequest(Integer ownerId, ShopToOrderableRequest request) {
        //대충 비즈니스 로직

    }
}
