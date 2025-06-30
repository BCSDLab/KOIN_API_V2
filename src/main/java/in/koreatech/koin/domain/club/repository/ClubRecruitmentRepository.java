package in.koreatech.koin.domain.club.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.ClubRecruitment;

public interface ClubRecruitmentRepository extends Repository<ClubRecruitment, Integer> {

    void save(ClubRecruitment clubRecruitment);
}
