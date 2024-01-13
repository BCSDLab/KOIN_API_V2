package in.koreatech.koin.domain.version.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.version.model.Version;

public interface VersionRepository extends Repository<Version, Long> {

    List<Version> findAll();

    Version save(Version version);

    void delete(Version version);

    Optional<Version> findByType(String type);

}
