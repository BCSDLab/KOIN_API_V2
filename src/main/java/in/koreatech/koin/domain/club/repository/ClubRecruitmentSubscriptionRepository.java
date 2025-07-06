package in.koreatech.koin.domain.club.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.model.ClubRecruitmentSubscription;

public interface ClubRecruitmentSubscriptionRepository extends Repository<ClubRecruitmentSubscription, Integer> {

    @Query("""
        SELECT s FROM ClubRecruitmentSubscription s
        JOIN FETCH s.user
        WHERE s.club.id = :clubId
        """)
    List<ClubRecruitmentSubscription> findAllWithUserByClubId(Integer clubId);
}
