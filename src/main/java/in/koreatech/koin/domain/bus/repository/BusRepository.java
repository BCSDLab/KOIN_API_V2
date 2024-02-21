package in.koreatech.koin.domain.bus.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.model.BusCourse;

public interface BusRepository extends Repository<BusCourse, String> {

    List<BusCourse> findByBusType(String busType);

    default List<BusCourse> getByBusType(String busType) {
        List<BusCourse> busCourses = findByBusType(busType);
        busCourses.removeIf(busCourse -> !busCourse.isRunning());
        return busCourses;
    }
}
