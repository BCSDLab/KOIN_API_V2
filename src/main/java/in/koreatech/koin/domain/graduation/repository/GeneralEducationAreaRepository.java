package in.koreatech.koin.domain.graduation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.GeneralEducationAreaNotFoundException;
import in.koreatech.koin.domain.graduation.model.GeneralEducationArea;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface GeneralEducationAreaRepository extends Repository<GeneralEducationArea, Integer> {

    Optional<GeneralEducationArea> findGeneralEducationAreaByName(String name);

    List<GeneralEducationArea> findAll();

    default GeneralEducationArea getGeneralEducationAreaByName(String name) {
        return findGeneralEducationAreaByName(name)
            .orElseThrow(() -> GeneralEducationAreaNotFoundException.withDetail("name" + name));
    }
}
