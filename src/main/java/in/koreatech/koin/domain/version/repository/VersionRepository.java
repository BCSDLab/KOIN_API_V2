package in.koreatech.koin.domain.version.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface VersionRepository extends Repository<Version, Integer> {

    Version save(Version version);

    Optional<Version> findByType(String type);

    default Version getByType(VersionType type) {
        return this.findByType(type.getValue())
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("versionType: " + type));
    }

    Optional<Version> findByTypeAndIsPrevious(String type, boolean isPrevious);

    default Version getByTypeAndIsPrevious(VersionType type, boolean isPrevious) {
        return this.findByTypeAndIsPrevious(type.getValue(), isPrevious)
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("versionType: " + type));
    }
}
