package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.global.exception.DataNotFoundException;

public interface OwnerAttachmentRepository extends Repository<OwnerAttachment, Integer> {

    OwnerAttachment save(OwnerAttachment ownerAttachment);

    Optional<OwnerAttachment> findById(Integer id);

    default OwnerAttachment getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> new DataNotFoundException("해당 첨부파일을 찾을 수 없습니다. id: " + id));
    }

    void deleteByOwnerId(Integer ownerId);
}
