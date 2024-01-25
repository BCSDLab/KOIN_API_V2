package in.koreatech.koin.domain.version.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.version.exception.VersionException;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;

public interface VersionRepository extends Repository<Version, Long> {

    Version save(Version version);

    Optional<Version> findByType(VersionType type);

    default Version getByType(VersionType type) {
        return this.findByType(type).orElseThrow(() -> VersionException.withDetail("versionType: " + type));
    }

}