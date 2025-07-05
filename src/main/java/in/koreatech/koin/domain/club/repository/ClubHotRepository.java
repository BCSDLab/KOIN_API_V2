package in.koreatech.koin.domain.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.ClubHot;

public interface ClubHotRepository extends Repository<ClubHot, Integer> {

    Optional<ClubHot> findTopByOrderByEndDateDesc();

    Optional<ClubHot> findTopByOrderByIdDesc();

    List<ClubHot> findAllByOrderByIdDesc();

    void save(ClubHot clubHot);
}
