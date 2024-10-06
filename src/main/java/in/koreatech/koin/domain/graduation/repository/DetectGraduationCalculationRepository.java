package in.koreatech.koin.domain.graduation.repository;

import in.koreatech.koin.domain.graduation.model.DetectGraduationCalculation;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface DetectGraduationCalculationRepository extends Repository<DetectGraduationCalculation, Integer> {

    Optional<DetectGraduationCalculation> findByUserId(Integer userId);
}
