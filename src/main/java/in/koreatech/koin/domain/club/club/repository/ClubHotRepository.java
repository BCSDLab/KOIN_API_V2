package in.koreatech.koin.domain.club.club.repository;

import in.koreatech.koin.domain.club.club.model.ClubHot;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface ClubHotRepository extends Repository<ClubHot, Integer> {

    Optional<ClubHot> findTopByOrderByEndDateDesc();

    Optional<ClubHot> findTopByClubIdOrderByIdDesc(Integer clubId);

    List<ClubHot> findAllByOrderByIdDesc();

    void save(ClubHot clubHot);
}
