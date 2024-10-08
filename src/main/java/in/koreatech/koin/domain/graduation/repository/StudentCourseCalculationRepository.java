package in.koreatech.koin.domain.graduation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;

@Repository
public interface StudentCourseCalculationRepository extends JpaRepository<StudentCourseCalculation, Integer> {

    Optional<StudentCourseCalculation> findByUserId(Integer userId);

    void deleteAllByUserId(Integer userId);
}
