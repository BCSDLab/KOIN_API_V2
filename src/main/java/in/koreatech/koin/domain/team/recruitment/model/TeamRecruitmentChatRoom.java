package in.koreatech.koin.domain.team.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "team_recruitment_chat_room",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_team_recruitment_chat_room_id_recruitment",
            columnNames = {"id", "recruitment_id"}
        ),
        @UniqueConstraint(
            name = "uk_team_recruitment_chat_room_recruitment_scope",
            columnNames = {"recruitment_id", "room_scope_key"}
        ),
        @UniqueConstraint(
            name = "uk_team_recruitment_chat_room_recruitment_application_type",
            columnNames = {"recruitment_id", "application_id", "room_type"}
        )
    }
)
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruitment_id", nullable = false, updatable = false)
    private TeamRecruitment recruitment;

    @NotNull
    @Size(max = 64)
    @Column(name = "room_scope_key", nullable = false, length = 64)
    private String roomScopeKey;

    @NotNull
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 16)
    private TeamRecruitmentChatRoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", updatable = false)
    private TeamRecruitmentApplication application;

    @NotNull
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TeamRecruitmentChatRoomStatus status;

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<TeamRecruitmentChatMember> members = new ArrayList<>();

    @Builder
    private TeamRecruitmentChatRoom(
        Integer id,
        TeamRecruitment recruitment,
        String roomScopeKey,
        TeamRecruitmentChatRoomType roomType,
        TeamRecruitmentApplication application,
        TeamRecruitmentChatRoomStatus status
    ) {
        this.id = id;
        this.recruitment = recruitment;
        this.roomScopeKey = roomScopeKey;
        this.roomType = roomType;
        this.application = application;
        this.status = status == null ? TeamRecruitmentChatRoomStatus.ACTIVE : status;
    }

    public void addMember(TeamRecruitmentChatMember member) {
        TeamRecruitmentChatMember nonNullMember = Objects.requireNonNull(member, "member must not be null");
        if (!members.contains(nonNullMember)) {
            members.add(nonNullMember);
        }
        nonNullMember.assignChatRoom(this);
    }

    public void removeMember(TeamRecruitmentChatMember member) {
        if (members.remove(member)) {
            member.assignChatRoom(null);
        }
    }

    public void markReadOnly() {
        this.status = TeamRecruitmentChatRoomStatus.READ_ONLY;
    }

    @Transient
    public boolean isActive() {
        return status == TeamRecruitmentChatRoomStatus.ACTIVE;
    }
}
