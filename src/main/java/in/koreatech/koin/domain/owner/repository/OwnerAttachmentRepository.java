package in.koreatech.koin.domain.owner.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.domain.OwnerAttachment;

public interface OwnerAttachmentRepository extends Repository<OwnerAttachment, Long> {

    OwnerAttachment save(OwnerAttachment ownerAttachment);

    OwnerAttachment findById(Long id);

    List<OwnerAttachment> findAllByOwnerId(Long ownerId);
}
