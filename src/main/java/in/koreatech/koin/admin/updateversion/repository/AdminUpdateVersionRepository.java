package in.koreatech.koin.admin.updateversion.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.updateversion.exception.UpdateVersionTypeNotFoundException;
import in.koreatech.koin.domain.updateversion.model.UpdateVersion;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;

public interface AdminUpdateVersionRepository extends Repository<UpdateVersion, Integer> {

    UpdateVersion save(UpdateVersion request);

    Optional<UpdateVersion> findByType(UpdateVersionType Type);

    Integer count();

    Page<UpdateVersion> findAll(Pageable pageable);

    default UpdateVersion getByType(UpdateVersionType type) {
        return findByType(type).orElseThrow(() -> UpdateVersionTypeNotFoundException.withDetail("type: " + type));
    }

    Optional<UpdateVersion> findById(Integer id);

    default UpdateVersion getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("id: " + id));
    }
}
