package in.koreatech.koin.domain.club.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.club.model.ClubHot;

public interface ClubHotRepository extends Repository<ClubHot, Integer> {

    Optional<ClubHot> findTopByOrderByEndDateDesc();

    Optional<ClubHot> findTopByClubIdOrderByIdDesc(Integer clubId);

    List<ClubHot> findAllByOrderByIdDesc();

    void save(ClubHot clubHot);
}
