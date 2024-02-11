package in.koreatech.koin.domain.owner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerResponse getOwnerShops(Long ownerId) {
        Owner foundOwner = ownerRepository.findById(ownerId);
        return OwnerResponse.from(foundOwner);
    }
}
