package in.koreatech.koin.domain.owner.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerAttachment;

public interface OwnerAttachmentRepository extends Repository<OwnerAttachment, Integer> {

    OwnerAttachment save(OwnerAttachment ownerAttachment);

    OwnerAttachment findById(Integer id);
}
