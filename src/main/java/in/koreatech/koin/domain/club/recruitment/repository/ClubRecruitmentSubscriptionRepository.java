package in.koreatech.koin.domain.club.recruitment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.club.recruitment.model.ClubRecruitmentSubscription;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface ClubRecruitmentSubscriptionRepository extends Repository<ClubRecruitmentSubscription, Integer> {

    @Query("""
        SELECT s FROM ClubRecruitmentSubscription s
        JOIN FETCH s.user
        WHERE s.club.id = :clubId
        """)
    List<ClubRecruitmentSubscription> findAllWithUserByClubId(Integer clubId);

    boolean existsByClubIdAndUserId(Integer clubId, Integer userId);

    void save(ClubRecruitmentSubscription subscription);

    void deleteByClubIdAndUserId(Integer clubId, Integer userId);
}
