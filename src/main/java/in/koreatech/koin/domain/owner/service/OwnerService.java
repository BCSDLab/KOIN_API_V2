package in.koreatech.koin.domain.owner.service;

import in.koreatech.koin.domain.owner.dto.CompanyNumberCheckRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisteredInfoResponse;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.exception.DuplicationCompanyNumberException;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final OwnerShopRedisRepository ownerShopRedisRepository;

    public OwnerResponse getOwner(Integer ownerId) {
        Owner foundOwner = ownerRepository.getById(ownerId);
        List<Shop> shops = shopRepository.findAllByOwnerId(ownerId);
        return OwnerResponse.of(foundOwner, foundOwner.getAttachments(), shops);
    }

    public void checkCompanyNumber(CompanyNumberCheckRequest request) {
        if (ownerRepository.findByCompanyRegistrationNumber(request.companyNumber()).isPresent()) {
            throw DuplicationCompanyNumberException.withDetail("companyNumber: " + request.companyNumber());
        }
    }

    public OwnerRegisteredInfoResponse getOwnerRegisteredStoreInfo(Integer userId) {
        OwnerShop ownerShop = ownerShopRedisRepository.findById(userId);
        return OwnerRegisteredInfoResponse.from(ownerShop);
    }
}
