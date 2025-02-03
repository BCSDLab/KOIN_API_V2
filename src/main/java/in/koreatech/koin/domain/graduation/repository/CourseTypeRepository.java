package in.koreatech.koin.domain.graduation.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.CourseTypeNotFoundException;
import in.koreatech.koin.domain.graduation.model.CourseType;

public interface CourseTypeRepository extends Repository<CourseType, Integer> {

    CourseType save(CourseType courseType);

    Optional<CourseType> findById(Integer id);

    Optional<CourseType> findByName(String name);

    default CourseType getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CourseTypeNotFoundException.withDetail("course_type_id: " + id));
    }

    default CourseType getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> CourseTypeNotFoundException.withDetail("course_type_name: " + name));
    }
}
