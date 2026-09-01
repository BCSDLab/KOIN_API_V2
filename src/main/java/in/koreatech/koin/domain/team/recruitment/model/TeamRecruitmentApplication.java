package in.koreatech.koin.domain.team.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "team_recruitment_application",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_team_recruitment_application_id_recruitment",
            columnNames = {"id", "recruitment_id"}
        ),
        @UniqueConstraint(
            name = "uk_team_recruitment_application_recruitment_applicant",
            columnNames = {"recruitment_id", "applicant_id"}
        )
    }
)
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruitment_id", nullable = false, updatable = false)
    private TeamRecruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false, updatable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", updatable = false)
    private TeamRecruitmentRole role;

    @NotNull
    @Size(max = 1000)
    @Column(name = "motivation", nullable = false, length = 1000)
    private String motivation;

    @NotNull
    @Size(max = 100)
    @Column(name = "availability", nullable = false, length = 100)
    private String availability;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TeamRecruitmentApplicationStatus status;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "profile_snapshot", nullable = false, columnDefinition = "JSON", updatable = false)
    private String profileSnapshot;

    @NotNull
    @Column(name = "snapshot_version", nullable = false)
    private Integer snapshotVersion;

    @Size(max = 32)
    @Column(name = "decision_reason", length = 32)
    private String decisionReason;

    @Builder
    private TeamRecruitmentApplication(
        Integer id,
        TeamRecruitment recruitment,
        User applicant,
        TeamRecruitmentRole role,
        String motivation,
        String availability,
        TeamRecruitmentApplicationStatus status,
        String profileSnapshot,
        Integer snapshotVersion,
        String decisionReason
    ) {
        this.id = id;
        this.recruitment = recruitment;
        this.applicant = applicant;
        this.role = role;
        this.motivation = motivation;
        this.availability = availability;
        this.status = status == null ? TeamRecruitmentApplicationStatus.PENDING : status;
        this.profileSnapshot = profileSnapshot;
        this.snapshotVersion = snapshotVersion == null ? 1 : snapshotVersion;
        this.decisionReason = decisionReason;
    }

    public void accept() {
        validatePendingStatus();
        this.status = TeamRecruitmentApplicationStatus.ACCEPTED;
        this.decisionReason = null;
    }

    public void reject(String decisionReason) {
        validatePendingStatus();
        this.status = TeamRecruitmentApplicationStatus.REJECTED;
        this.decisionReason = decisionReason;
    }

    private void validatePendingStatus() {
        if (this.status != TeamRecruitmentApplicationStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 팀 모집 지원서는 상태를 변경할 수 없습니다.");
        }
    }
}
