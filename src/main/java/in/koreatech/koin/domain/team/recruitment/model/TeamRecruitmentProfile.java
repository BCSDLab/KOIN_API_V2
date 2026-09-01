package in.koreatech.koin.domain.team.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "team_recruitment_profile")
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentProfile extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private Integer userId;

    @OneToOne(fetch = LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @NotNull
    @Size(max = 20)
    @Column(name = "profile_nickname", nullable = false, length = 20)
    private String profileNickname;

    @NotNull
    @Size(max = 20)
    @Column(name = "preferred_role", nullable = false, length = 20)
    private String preferredRole;

    @NotNull
    @Size(max = 1000)
    @Column(name = "self_introduction", nullable = false, length = 1000)
    private String selfIntroduction;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<TeamRecruitmentProfileSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<TeamRecruitmentProfileActivity> activities = new ArrayList<>();

    @Builder
    private TeamRecruitmentProfile(
        Integer userId,
        User user,
        String profileNickname,
        String preferredRole,
        String selfIntroduction
    ) {
        this.userId = userId;
        this.user = user;
        this.profileNickname = profileNickname;
        this.preferredRole = preferredRole;
        this.selfIntroduction = selfIntroduction;
    }

    public void addSkill(TeamRecruitmentProfileSkill skill) {
        TeamRecruitmentProfileSkill nonNullSkill = Objects.requireNonNull(skill, "skill must not be null");
        if (!skills.contains(nonNullSkill)) {
            skills.add(nonNullSkill);
        }
        nonNullSkill.assignProfile(this);
    }

    public void addActivity(TeamRecruitmentProfileActivity activity) {
        TeamRecruitmentProfileActivity nonNullActivity = Objects.requireNonNull(activity, "activity must not be null");
        if (!activities.contains(nonNullActivity)) {
            activities.add(nonNullActivity);
        }
        nonNullActivity.assignProfile(this);
    }

    public void replace(
        String profileNickname,
        String preferredRole,
        String selfIntroduction
    ) {
        this.profileNickname = profileNickname;
        this.preferredRole = preferredRole;
        this.selfIntroduction = selfIntroduction;
    }

    public void replaceSkills(List<TeamRecruitmentProfileSkill> newSkills) {
        skills.forEach(skill -> skill.assignProfile(null));
        skills.clear();
        if (newSkills != null) {
            newSkills.forEach(this::addSkill);
        }
    }

    public void replaceActivities(List<TeamRecruitmentProfileActivity> newActivities) {
        activities.forEach(activity -> activity.assignProfile(null));
        activities.clear();
        if (newActivities != null) {
            newActivities.forEach(this::addActivity);
        }
    }
}
