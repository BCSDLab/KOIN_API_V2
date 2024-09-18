package in.koreatech.koin.domain.updateversion.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.updateversion.model.UpdateContent;
import in.koreatech.koin.domain.updateversion.model.UpdateVersion;

public interface UpdateContentRepository extends Repository<UpdateContent, Integer> {

    UpdateContent save(UpdateVersion version);

    Optional<UpdateContent> findById(Integer id);
}
