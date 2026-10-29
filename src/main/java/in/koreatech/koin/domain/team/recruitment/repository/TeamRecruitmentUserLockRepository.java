package in.koreatech.koin.domain.team.recruitment.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentUserLock;

public interface TeamRecruitmentUserLockRepository extends Repository<TeamRecruitmentUserLock, Integer> {

    // 모집글 잠금보다 먼저 획득하고, 같은 트랜잭션이 끝날 때까지 유지한다.
    // users FK를 두면 승인 중 사용자 참조를 추가하는 작업과 잠금 순서가 역전될 수 있다.
    @Modifying
    @Query(value = """
        INSERT INTO team_recruitment_user_lock (user_id) VALUES (:userId)
        ON DUPLICATE KEY UPDATE user_id = :userId
        """, nativeQuery = true)
    void lock(@Param("userId") Integer userId);

    @Modifying
    @Query(value = "DELETE FROM team_recruitment_user_lock WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Integer userId);
}
