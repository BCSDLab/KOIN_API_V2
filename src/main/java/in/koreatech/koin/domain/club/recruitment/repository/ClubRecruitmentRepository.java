package in.koreatech.koin.domain.club.recruitment.repository;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.recruitment.model.ClubRecruitment;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface ClubRecruitmentRepository extends Repository<ClubRecruitment, Integer> {

    void save(ClubRecruitment clubRecruitment);

    Optional<ClubRecruitment> findByClub(Club club);

    default ClubRecruitment getByClub(Club club) {
        return findByClub(club)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_CLUB_RECRUITMENT));
    }

    void delete(ClubRecruitment clubRecruitment);
}
