package in.koreatech.koin.domain.community.articles.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "boards")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 10)
    @NotNull
    @Column(name = "tag", nullable = false, length = 10, unique = true)
    private String tag;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotNull
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    @NotNull
    @Column(name = "article_count", nullable = false)
    private Integer articleCount;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @NotNull
    @Column(name = "is_notice", nullable = false)
    private boolean isNotice = false;

    @Column(name = "parent_id")
    private Integer parentId;

    @NotNull
    @Column(name = "seq", nullable = false)
    private Integer seq;

    public List<Board> getChildren() {
        return new ArrayList<>();
    }

    @Builder
    private Board(
        String tag,
        String name,
        boolean isAnonymous,
        Integer articleCount,
        boolean isDeleted,
        boolean isNotice,
        Integer parentId,
        Integer seq
    ) {
        this.tag = tag;
        this.name = name;
        this.isAnonymous = isAnonymous;
        this.articleCount = articleCount;
        this.isDeleted = isDeleted;
        this.isNotice = isNotice;
        this.parentId = parentId;
        this.seq = seq;
    }
}
