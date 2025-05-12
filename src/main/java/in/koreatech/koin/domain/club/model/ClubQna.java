package in.koreatech.koin.domain.club.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "club_qna")
@NoArgsConstructor(access = PROTECTED)
public class ClubQna extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @JoinColumn(name = "club_id", nullable = false)
    @ManyToOne(fetch = LAZY)
    private Club club;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private ClubQna parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubQna> children = new ArrayList<>();

    @NotNull
    @Column(nullable = false)
    private String content;

    @NotNull
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = FALSE;

    @Builder
    private ClubQna(
        Integer id,
        Club club,
        User user,
        ClubQna parent,
        String content,
        Boolean isDeleted
    ) {
        this.id = id;
        this.club = club;
        this.user = user;
        this.parent = parent;
        this.content = content;
        this.isDeleted = isDeleted;
    }

    public void delete() {
        isDeleted = true;
    }

    public boolean isRoot() {
        return parent == null;
    }
}
