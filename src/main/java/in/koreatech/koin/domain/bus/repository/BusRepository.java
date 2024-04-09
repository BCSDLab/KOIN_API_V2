package in.koreatech.koin.domain.bus.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.model.mongo.BusCourse;

public interface BusRepository extends Repository<BusCourse, String> {

    BusCourse save(BusCourse busCourse);

    List<BusCourse> findByBusType(String busType);

    BusCourse findByBusTypeAndDirectionAndRegion(String busType, String direction, String region);

    @Query(value = "{ 'bus_type': ?0, 'direction': ?1, 'region': ?2}")
    BusCourse findByCourse(String busType, String direction, String region);
}
