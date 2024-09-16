package in.koreatech.koin.domain.mobileversion.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.mobileversion.model.MobileVersion;
import in.koreatech.koin.domain.mobileversion.exception.MobileVersionTypeNotFoundException;
import in.koreatech.koin.domain.version.model.VersionType;

public interface MobileVersionRepository extends Repository<MobileVersion, Integer> {

    MobileVersion save(MobileVersion version);

    Optional<MobileVersion> findByType(String type);

    default MobileVersion getByType(VersionType type) {
        return this.findByType(type.getValue())
            .orElseThrow(() -> MobileVersionTypeNotFoundException.withDetail("versionType: " + type));
    }
}
