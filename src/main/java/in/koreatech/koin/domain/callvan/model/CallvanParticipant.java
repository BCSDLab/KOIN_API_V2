package in.koreatech.koin.domain.callvan.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.callvan.model.enums.CallvanRole;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "callvan_participant")
@NoArgsConstructor(access = PROTECTED)
public class CallvanParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CallvanPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private CallvanRole role = CallvanRole.PARTICIPANT;

    @Column(name = "joined_at", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public void joinCallvanAgain() {
        this.isDeleted = false;
    }

    public void leaveCallvan() {
        this.isDeleted = true;
    }

    @Builder
    private CallvanParticipant(
            CallvanPost post,
            User member,
            CallvanRole role,
            LocalDateTime joinedAt,
            Boolean isDeleted
    ) {
        this.post = post;
        this.member = member;
        this.role = role != null ? role : CallvanRole.PARTICIPANT;
        this.joinedAt = joinedAt != null ? joinedAt : LocalDateTime.now();
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }
}
