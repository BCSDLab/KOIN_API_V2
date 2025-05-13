package in.koreatech.koin.domain.club.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.ClubHot;

public interface ClubHotRepository extends Repository<ClubHot, Integer> {

    Optional<ClubHot> findTopByOrderByEndDateDesc();

    void save(ClubHot clubHot);
}
