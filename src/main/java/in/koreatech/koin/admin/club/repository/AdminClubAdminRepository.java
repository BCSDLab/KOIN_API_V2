package in.koreatech.koin.admin.club.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubAdmin;

public interface AdminClubAdminRepository extends Repository<ClubAdmin, Integer> {

    void saveAll(Iterable<ClubAdmin> clubAdmins);

    ClubAdmin save(ClubAdmin clubAdmin);

    void deleteAllByClub(Club club);
}
