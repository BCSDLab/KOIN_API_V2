package in.koreatech.koin.domain.team.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "team_recruitment_outbox_event",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_team_recruitment_outbox_event_key",
        columnNames = "event_key"
    )
)
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentOutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @NotNull
    @Size(max = 255)
    @Column(name = "event_key", nullable = false, length = 255, updatable = false)
    private String eventKey;

    @NotNull
    @Size(max = 64)
    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @NotNull
    @Size(max = 64)
    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @NotNull
    @Column(name = "aggregate_id", nullable = false, updatable = false)
    private Integer aggregateId;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "JSON", updatable = false)
    private String payload;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TeamRecruitmentOutboxEventStatus status;

    @NotNull
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "next_attempt_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime nextAttemptAt;

    @Column(name = "locked_until", columnDefinition = "TIMESTAMP")
    private LocalDateTime lockedUntil;

    @Size(max = 100)
    @Column(name = "worker_id", length = 100)
    private String workerId;

    @Column(name = "published_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime publishedAt;

    @Size(max = 500)
    @Column(name = "last_error", length = 500)
    private String lastError;

    @Builder
    private TeamRecruitmentOutboxEvent(
        Integer id,
        String eventKey,
        String eventType,
        String aggregateType,
        Integer aggregateId,
        String payload,
        TeamRecruitmentOutboxEventStatus status,
        Integer attemptCount,
        LocalDateTime nextAttemptAt,
        LocalDateTime lockedUntil,
        String workerId,
        LocalDateTime publishedAt,
        String lastError
    ) {
        this.id = id;
        this.eventKey = eventKey;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = status == null ? TeamRecruitmentOutboxEventStatus.PENDING : status;
        this.attemptCount = attemptCount == null ? 0 : attemptCount;
        this.nextAttemptAt = nextAttemptAt;
        this.lockedUntil = lockedUntil;
        this.workerId = workerId;
        this.publishedAt = publishedAt;
        this.lastError = lastError;
    }

    public void markProcessing() {
        markProcessing(null, null);
    }

    public void markProcessing(String workerId, LocalDateTime lockedUntil) {
        this.status = TeamRecruitmentOutboxEventStatus.PROCESSING;
        this.attemptCount++;
        this.workerId = workerId;
        this.lockedUntil = lockedUntil;
    }

    public boolean isProcessingBy(String workerId) {
        return status == TeamRecruitmentOutboxEventStatus.PROCESSING
            && java.util.Objects.equals(this.workerId, workerId);
    }

    public void markPublished(LocalDateTime publishedAt) {
        this.status = TeamRecruitmentOutboxEventStatus.PUBLISHED;
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt must not be null");
        this.nextAttemptAt = null;
        this.lockedUntil = null;
        this.workerId = null;
        this.lastError = null;
    }

    public void markFailed(String lastError, LocalDateTime nextAttemptAt) {
        this.status = TeamRecruitmentOutboxEventStatus.FAILED;
        this.lastError = lastError;
        this.nextAttemptAt = nextAttemptAt;
        this.lockedUntil = null;
        this.workerId = null;
    }

    public void markTerminalFailure(String lastError, int maxAttempts) {
        this.status = TeamRecruitmentOutboxEventStatus.FAILED;
        this.attemptCount = Math.max(this.attemptCount, maxAttempts);
        this.lastError = lastError;
        this.nextAttemptAt = null;
        this.lockedUntil = null;
        this.workerId = null;
    }
}
