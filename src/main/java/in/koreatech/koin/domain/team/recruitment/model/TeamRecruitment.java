package in.koreatech.koin.domain.team.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "team_recruitment")
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false, updatable = false)
    private User author;

    @NotNull
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private TeamRecruitmentCategory category;

    @NotNull
    @Size(max = 50)
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @NotNull
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "meeting_type", nullable = false, length = 16)
    private TeamRecruitmentMeetingType meetingType;

    @NotNull
    @Column(name = "activity_start_date", nullable = false)
    private LocalDate activityStartDate;

    @NotNull
    @Column(name = "activity_end_date", nullable = false)
    private LocalDate activityEndDate;

    @NotNull
    @Column(name = "deadline_date", nullable = false)
    private LocalDate deadlineDate;

    @NotNull
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "recruitment_type", nullable = false, length = 16)
    private TeamRecruitmentType recruitmentType;

    @NotNull
    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @NotNull
    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants;

    @NotNull
    @Size(max = 1000)
    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Size(max = 2048)
    @Column(name = "related_url", length = 2048)
    private String relatedUrl;

    @Size(max = 500)
    @Column(name = "qualification", length = 500)
    private String qualification;

    @NotNull
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TeamRecruitmentStatus status;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "recruitment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<TeamRecruitmentRole> roles = new ArrayList<>();

    @Builder
    private TeamRecruitment(
        Integer id,
        User author,
        TeamRecruitmentCategory category,
        String title,
        TeamRecruitmentMeetingType meetingType,
        LocalDate activityStartDate,
        LocalDate activityEndDate,
        LocalDate deadlineDate,
        TeamRecruitmentType recruitmentType,
        Integer maxParticipants,
        Integer currentParticipants,
        String description,
        String relatedUrl,
        String qualification,
        TeamRecruitmentStatus status,
        LocalDateTime deletedAt
    ) {
        this.id = id;
        this.author = author;
        this.category = category;
        this.title = title;
        this.meetingType = meetingType;
        this.activityStartDate = activityStartDate;
        this.activityEndDate = activityEndDate;
        this.deadlineDate = deadlineDate;
        this.recruitmentType = recruitmentType;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants == null ? 0 : currentParticipants;
        this.description = description;
        this.relatedUrl = relatedUrl;
        this.qualification = qualification;
        this.status = status == null ? TeamRecruitmentStatus.RECRUITING : status;
        this.deletedAt = deletedAt;
        validateDeletionState();
    }

    public void addRole(TeamRecruitmentRole role) {
        TeamRecruitmentRole nonNullRole = Objects.requireNonNull(role, "role must not be null");
        if (!roles.contains(nonNullRole)) {
            roles.add(nonNullRole);
        }
        nonNullRole.assignRecruitment(this);
    }

    public void removeRole(TeamRecruitmentRole role) {
        if (roles.remove(role)) {
            role.assignRecruitment(null);
        }
    }

    public void clearRoles() {
        roles.forEach(role -> role.assignRecruitment(null));
        roles.clear();
    }

    public void modify(
        TeamRecruitmentCategory category,
        String title,
        TeamRecruitmentMeetingType meetingType,
        LocalDate activityStartDate,
        LocalDate activityEndDate,
        LocalDate deadlineDate,
        TeamRecruitmentType recruitmentType,
        Integer maxParticipants,
        String description,
        String relatedUrl,
        String qualification
    ) {
        this.category = category;
        this.title = title;
        this.meetingType = meetingType;
        this.activityStartDate = activityStartDate;
        this.activityEndDate = activityEndDate;
        this.deadlineDate = deadlineDate;
        this.recruitmentType = recruitmentType;
        this.maxParticipants = maxParticipants;
        this.description = description;
        this.relatedUrl = relatedUrl;
        this.qualification = qualification;
    }

    public void increaseCurrentParticipants() {
        if (this.currentParticipants >= this.maxParticipants) {
            throw new IllegalStateException("모집 인원을 초과하여 승인할 수 없습니다.");
        }
        this.currentParticipants++;
    }

    public void decreaseCurrentParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    public void close() {
        if (isDeleted()) {
            throw new IllegalStateException("삭제된 모집글은 마감할 수 없습니다.");
        }
        this.status = TeamRecruitmentStatus.CLOSED;
    }

    public void markDeleted(LocalDateTime deletedAt) {
        LocalDateTime requiredDeletedAt = Objects.requireNonNull(deletedAt, "deletedAt must not be null");
        this.status = TeamRecruitmentStatus.DELETED;
        this.deletedAt = requiredDeletedAt;
    }

    @Transient
    public boolean isRecruiting() {
        return status == TeamRecruitmentStatus.RECRUITING;
    }

    @Transient
    public boolean isDeleted() {
        return status == TeamRecruitmentStatus.DELETED;
    }

    private void validateDeletionState() {
        boolean deleted = status == TeamRecruitmentStatus.DELETED;
        if (deleted != (deletedAt != null)) {
            throw new IllegalArgumentException("삭제 상태와 삭제 시각이 일치해야 합니다.");
        }
    }
}
