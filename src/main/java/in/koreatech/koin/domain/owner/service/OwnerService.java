package in.koreatech.koin.domain.owner.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.domain.OwnerAttachment;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.repository.OwnerAttachmentRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final ShopRepository shopRepository;
    private final OwnerRepository ownerRepository;
    private final OwnerAttachmentRepository ownerAttachmentRepository;

    public OwnerResponse getOwnerShops(Long ownerId) {
        Owner foundOwner = ownerRepository.getById(ownerId);
        List<OwnerAttachment> attachments = ownerAttachmentRepository.findAllByOwnerId(ownerId);
        List<Shop> shops = shopRepository.findAllByOwner(foundOwner);
        return OwnerResponse.of(foundOwner, attachments, shops);
    }
}
