package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.exception.OwnerAttachmentNotFoundException;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;

public interface OwnerAttachmentRepository extends Repository<OwnerAttachment, Integer> {

    OwnerAttachment save(OwnerAttachment ownerAttachment);

    Optional<OwnerAttachment> findById(Integer id);

    default OwnerAttachment getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> OwnerAttachmentNotFoundException.withDetail("id: " + id));
    }

    void deleteByOwnerId(Integer ownerId);
}
