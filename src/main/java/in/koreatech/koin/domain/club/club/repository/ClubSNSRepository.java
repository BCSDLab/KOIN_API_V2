package in.koreatech.koin.domain.club.club.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.model.ClubSNS;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface ClubSNSRepository extends Repository<ClubSNS, Integer> {

    void save(ClubSNS clubSNS);

    List<ClubSNS> findAllByClub(Club club);

    void deleteAllByClub(Club club);
}
