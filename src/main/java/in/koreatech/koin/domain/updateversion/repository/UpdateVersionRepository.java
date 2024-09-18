package in.koreatech.koin.domain.updateversion.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.updateversion.exception.UpdateVersionTypeNotFoundException;
import in.koreatech.koin.domain.updateversion.model.UpdateVersion;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;

public interface UpdateVersionRepository extends Repository<UpdateVersion, Integer> {

    UpdateVersion save(UpdateVersion version);

    Optional<UpdateVersion> findByType(UpdateVersionType type);

    default UpdateVersion getByType(UpdateVersionType type) {
        return this.findByType(type)
            .orElseThrow(() -> UpdateVersionTypeNotFoundException.withDetail("versionType: " + type));
    }
}
