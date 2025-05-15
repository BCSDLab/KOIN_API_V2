package in.koreatech.koin.admin.club.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubSNS;

public interface AdminClubSnsRepository extends Repository<ClubSNS, Integer> {

    void saveAll(Iterable<ClubSNS> clubSNSs);

    void deleteAllByClub(Club club);
}
