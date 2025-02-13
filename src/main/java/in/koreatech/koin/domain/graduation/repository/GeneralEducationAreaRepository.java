package in.koreatech.koin.domain.graduation.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.GeneralEducationAreaNotFoundException;
import in.koreatech.koin.domain.graduation.model.GeneralEducationArea;

public interface GeneralEducationAreaRepository extends Repository<GeneralEducationArea, Integer> {

    Optional<GeneralEducationArea> findGeneralEducationAreaByName(String name);

    default GeneralEducationArea getGeneralEducationAreaByName(String name) {
        return findGeneralEducationAreaByName(name)
            .orElseThrow(() -> GeneralEducationAreaNotFoundException.withDetail("name" + name));
    }
}
