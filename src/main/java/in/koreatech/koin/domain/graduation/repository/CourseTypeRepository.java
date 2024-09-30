package in.koreatech.koin.domain.graduation.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.CourseTypeNotFoundException;
import in.koreatech.koin.domain.graduation.model.CourseType;

public interface CourseTypeRepository extends Repository<CourseType, Integer> {

    Optional<CourseType> findById(Integer id);

    default CourseType getCourseTypeById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CourseTypeNotFoundException.withDetail("course_type_id: " + id));
    }
}
