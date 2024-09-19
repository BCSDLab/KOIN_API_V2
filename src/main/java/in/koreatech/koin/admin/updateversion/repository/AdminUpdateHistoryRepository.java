package in.koreatech.koin.admin.updateversion.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.updateversion.model.UpdateVersionHistory;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;

public interface AdminUpdateHistoryRepository extends Repository<UpdateVersionHistory, Integer> {

    UpdateVersionHistory save(UpdateVersionHistory request);

    Integer countByType(UpdateVersionType type);

    Page<UpdateVersionHistory> findAll(Pageable pageable);

    Optional<UpdateVersionHistory> findById(Integer id);

    default UpdateVersionHistory getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("id: " + id));
    }

    Page<UpdateVersionHistory> findAllByType(UpdateVersionType type, PageRequest pageRequest);
}
