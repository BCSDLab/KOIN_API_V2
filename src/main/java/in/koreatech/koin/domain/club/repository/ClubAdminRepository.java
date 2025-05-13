package in.koreatech.koin.domain.club.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.ClubAdmin;

public interface ClubAdminRepository extends Repository<ClubAdmin, Integer> {

    boolean existsByClubIdAndUserId(Integer clubId, Integer studentId);
}
