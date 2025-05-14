package in.koreatech.koin.domain.club.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.exception.ClubNotFoundException;
import in.koreatech.koin.domain.club.model.ClubAdmin;

public interface ClubAdminRepository extends Repository<ClubAdmin, Integer> {

    boolean existsByClubIdAndUserId(Integer clubId, Integer studentId);

    Optional<ClubAdmin> findByClubId(Integer clubId);

    default ClubAdmin getByClubId(Integer clubId) {
        return findByClubId(clubId).orElseThrow(() -> ClubNotFoundException.withDetail("Club Id :" + clubId));
    }
}
