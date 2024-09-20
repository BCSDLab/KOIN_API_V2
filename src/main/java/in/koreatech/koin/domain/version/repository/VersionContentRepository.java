package in.koreatech.koin.domain.version.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionContent;

public interface VersionContentRepository extends Repository<VersionContent, Integer> {

    VersionContent save(Version version);

    Optional<VersionContent> findById(Integer id);

    default VersionContent getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("id: " + id));
    }
}
