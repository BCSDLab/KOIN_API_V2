package in.koreatech.koin.admin.club.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubManager;

public interface AdminClubManagerRepository extends Repository<ClubManager, Integer> {

    void saveAll(Iterable<ClubManager> clubAdmins);

    ClubManager save(ClubManager clubManager);

    void deleteAllByClub(Club club);
}
