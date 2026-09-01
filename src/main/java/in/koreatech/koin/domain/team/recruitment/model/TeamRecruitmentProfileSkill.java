package in.koreatech.koin.domain.team.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "team_recruitment_profile_skill",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_team_recruitment_profile_skill_user_order",
        columnNames = {"profile_user_id", "display_order"}
    )
)
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentProfileSkill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_user_id", nullable = false, updatable = false)
    private TeamRecruitmentProfile profile;

    @NotNull
    @Size(max = 20)
    @Column(name = "skill", nullable = false, length = 20)
    private String skill;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    private TeamRecruitmentProfileSkill(
        Integer id,
        TeamRecruitmentProfile profile,
        String skill,
        Integer displayOrder
    ) {
        this.id = id;
        this.profile = profile;
        this.skill = skill;
        this.displayOrder = displayOrder;
    }

    void assignProfile(TeamRecruitmentProfile profile) {
        this.profile = profile;
    }

    public void modify(String skill, Integer displayOrder) {
        this.skill = skill;
        this.displayOrder = displayOrder;
    }
}
