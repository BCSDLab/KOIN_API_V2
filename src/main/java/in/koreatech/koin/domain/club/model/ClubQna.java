package in.koreatech.koin.domain.club.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.FALSE;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.student.model.Student;
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

    @JoinColumn(name = "author_id")
    @ManyToOne(fetch = LAZY)
    private Student author;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private ClubQna parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClubQna> children = new ArrayList<>();

    @NotNull
    @Column(nullable = false)
    private String content;

    @NotNull
    @Column(name = "is_manager", nullable = false)
    private Boolean isManager;

    @NotNull
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = FALSE;

    @Builder
    private ClubQna(
        Integer id,
        Club club,
        Student author,
        ClubQna parent,
        String content,
        Boolean isManager,
        Boolean isDeleted
    ) {
        this.id = id;
        this.club = club;
        this.author = author;
        this.parent = parent;
        this.content = content;
        this.isManager = isManager;
        this.isDeleted = isDeleted;
    }

    public void removeChild(ClubQna child) {
        children.remove(child);
        child.parent = null;
    }

    public void delete() {
        isDeleted = true;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isChild() {
        return parent != null;
    }

    public void detachFromParentIfChild() {
        if (isChild()) {
            parent.removeChild(this);
        }
    }
}
