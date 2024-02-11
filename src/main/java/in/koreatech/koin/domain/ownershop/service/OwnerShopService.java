package in.koreatech.koin.domain.ownershop.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopsResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerShopService {

    private final OwnerRepository ownerRepository;

    public OwnerShopsResponse getOwnerShops(Long ownerId) {
        Owner foundOwner = ownerRepository.getById(ownerId);
        return OwnerShopsResponse.from(foundOwner);
    }
}
