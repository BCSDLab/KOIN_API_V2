package in.koreatech.koin.admin.updateversion.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.updateversion.model.UpdateHistory;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;

public interface AdminUpdateHistoryRepository extends Repository<UpdateHistory, Integer> {

    UpdateHistory save(UpdateHistory request);

    Optional<UpdateHistory> findByType(String Type);

    Integer countByType(UpdateVersionType type);

    Page<UpdateHistory> findAll(Pageable pageable);

    default UpdateHistory getByType(String type) {
        return findByType(type).orElseThrow(() -> VersionTypeNotFoundException.withDetail("type: " + type));
    }

    Optional<UpdateHistory> findById(Integer id);

    default UpdateHistory getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("id: " + id));
    }

    Page<UpdateHistory> findAllByType(UpdateVersionType type, PageRequest pageRequest);
}
