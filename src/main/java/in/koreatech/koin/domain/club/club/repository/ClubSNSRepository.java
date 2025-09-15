package in.koreatech.koin.domain.club.club.repository;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.model.ClubSNS;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ClubSNSRepository extends Repository<ClubSNS, Integer> {

    void save(ClubSNS clubSNS);

    List<ClubSNS> findAllByClub(Club club);

    void deleteAllByClub(Club club);
}
