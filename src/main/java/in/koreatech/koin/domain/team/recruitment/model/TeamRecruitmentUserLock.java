package in.koreatech.koin.domain.team.recruitment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_recruitment_user_lock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamRecruitmentUserLock {

    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;
}
