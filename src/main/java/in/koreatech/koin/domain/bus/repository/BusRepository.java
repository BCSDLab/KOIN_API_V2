package in.koreatech.koin.domain.bus.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.model.BusCourse;

public interface BusRepository extends Repository<BusCourse, String> {

    List<BusCourse> findAll();

    List<BusCourse> findByBusTypeAndDirection(String busType, String direction);

    default List<BusCourse> getByBusTypeAndDirection(String busType, String direction) {
        List<BusCourse> busCourses = findByBusTypeAndDirection(busType, direction);
        busCourses.removeIf(busCourse -> !busCourse.isRunning());
        return busCourses;
    }
}
