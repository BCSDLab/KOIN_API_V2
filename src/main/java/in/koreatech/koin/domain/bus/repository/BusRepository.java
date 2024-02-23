package in.koreatech.koin.domain.bus.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.model.BusCourse;
import in.koreatech.koin.domain.bus.model.BusType;

public interface BusRepository extends Repository<BusCourse, String> {

    BusCourse save(BusCourse busCourse);

    List<BusCourse> findByBusType(String busType);

    default List<BusCourse> getByBusType(BusType busType) {
        return findByBusType(busType.getName());
    }
}
