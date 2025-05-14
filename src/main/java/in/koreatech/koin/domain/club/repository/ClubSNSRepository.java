package in.koreatech.koin.domain.club.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubSNS;

public interface ClubSNSRepository extends Repository<ClubSNS, Integer> {

    void save(ClubSNS clubSNS);

    List<ClubSNS> findAllByClub(Club club);

    void deleteAllByClub(Club club);
}
