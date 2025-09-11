package in.koreatech.koin.admin.club.repository;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.model.ClubSNS;
import org.springframework.data.repository.Repository;

public interface AdminClubSnsRepository extends Repository<ClubSNS, Integer> {

    void saveAll(Iterable<ClubSNS> clubSNSs);

    void deleteAllByClub(Club club);
}
