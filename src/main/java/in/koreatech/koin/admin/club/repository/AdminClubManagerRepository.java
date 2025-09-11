package in.koreatech.koin.admin.club.repository;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.model.ClubManager;
import org.springframework.data.repository.Repository;

public interface AdminClubManagerRepository extends Repository<ClubManager, Integer> {

    void saveAll(Iterable<ClubManager> clubAdmins);

    ClubManager save(ClubManager clubManager);

    void deleteAllByClub(Club club);
}
