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

import java.time.LocalDate;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "team_recruitment_profile_activity",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_team_recruitment_profile_activity_user_order",
        columnNames = {"profile_user_id", "display_order"}
    )
)
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentProfileActivity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_user_id", nullable = false, updatable = false)
    private TeamRecruitmentProfile profile;

    @NotNull
    @Size(max = 50)
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @NotNull
    @Column(name = "started_at", nullable = false)
    private LocalDate startedAt;

    @Column(name = "ended_at")
    private LocalDate endedAt;

    @NotNull
    @Column(name = "is_ongoing", nullable = false)
    private Boolean isOngoing;

    @NotNull
    @Size(max = 500)
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    private TeamRecruitmentProfileActivity(
        Integer id,
        TeamRecruitmentProfile profile,
        String title,
        LocalDate startedAt,
        LocalDate endedAt,
        Boolean isOngoing,
        String description,
        Integer displayOrder
    ) {
        this.id = id;
        this.profile = profile;
        this.title = title;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.isOngoing = isOngoing == null ? Boolean.FALSE : isOngoing;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    void assignProfile(TeamRecruitmentProfile profile) {
        this.profile = profile;
    }

    public void modify(
        String title,
        LocalDate startedAt,
        LocalDate endedAt,
        Boolean isOngoing,
        String description,
        Integer displayOrder
    ) {
        this.title = title;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.isOngoing = isOngoing;
        this.description = description;
        this.displayOrder = displayOrder;
    }
}
