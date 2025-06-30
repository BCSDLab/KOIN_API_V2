package in.koreatech.koin.domain.club.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.exception.ClubRecruitmentNotFoundException;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubRecruitment;

public interface ClubRecruitmentRepository extends Repository<ClubRecruitment, Integer> {

    void save(ClubRecruitment clubRecruitment);

    Optional<ClubRecruitment> findByClub(Club club);

    default ClubRecruitment getByClub(Club club) {
        return findByClub(club)
            .orElseThrow(() -> ClubRecruitmentNotFoundException.withDetail("clubId : " + club.getId()));
    }

    void delete(ClubRecruitment clubRecruitment);
}
