package in.koreatech.koin.domain.bus.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.model.mongo.BusCourse;

public interface BusRepository extends Repository<BusCourse, String> {

    BusCourse save(BusCourse busCourse);

    List<BusCourse> findByBusType(String busType);
}
