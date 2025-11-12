package in.koreatech.koin.domain.graduation.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.model.DetectGraduationCalculation;

public interface DetectGraduationCalculationRepository extends Repository<DetectGraduationCalculation, Integer> {

    void save(DetectGraduationCalculation detectGraduationCalculation);

    Optional<DetectGraduationCalculation> findByUserId(Integer userId);
}

