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
import jakarta.persistence.Transient;
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
    name = "team_recruitment_role",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_team_recruitment_role_recruitment_name",
            columnNames = {"recruitment_id", "name"}
        ),
        @UniqueConstraint(
            name = "uk_team_recruitment_role_recruitment_order",
            columnNames = {"recruitment_id", "display_order"}
        )
    }
)
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruitment_id", nullable = false, updatable = false)
    private TeamRecruitment recruitment;

    @NotNull
    @Size(max = 10)
    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @NotNull
    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @NotNull
    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    private TeamRecruitmentRole(
        Integer id,
        TeamRecruitment recruitment,
        String name,
        Integer maxParticipants,
        Integer currentParticipants,
        Integer displayOrder
    ) {
        this.id = id;
        this.recruitment = recruitment;
        this.name = name;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants == null ? 0 : currentParticipants;
        this.displayOrder = displayOrder;
    }

    void assignRecruitment(TeamRecruitment recruitment) {
        this.recruitment = recruitment;
    }

    public void modify(String name, Integer maxParticipants, Integer displayOrder) {
        if (maxParticipants == null || maxParticipants < currentParticipants) {
            throw new IllegalArgumentException("역할 모집 인원은 현재 승인 인원보다 작을 수 없습니다.");
        }
        this.name = name;
        this.maxParticipants = maxParticipants;
        this.displayOrder = displayOrder;
    }

    public void increaseCurrentParticipants() {
        if (isClosed()) {
            throw new IllegalStateException("역할 모집 인원을 초과하여 승인할 수 없습니다.");
        }
        this.currentParticipants++;
    }

    public void decreaseCurrentParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    @Transient
    public boolean isClosed() {
        return currentParticipants >= maxParticipants;
    }
}
