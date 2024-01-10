package in.koreatech.koin.domain.community.model;

import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "boards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 10)
    @NotNull
    @Column(name = "tag", nullable = false, length = 10)
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
    private Long articleCount;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @NotNull
    @Column(name = "is_notice", nullable = false)
    private Boolean isNotice = false;

    @Column(name = "parent_id")
    private Long parentId;

    @NotNull
    @Column(name = "seq", nullable = false)
    private Long seq;

    @Builder
    private Board(String tag, String name, Boolean isAnonymous, Long articleCount, Boolean isDeleted,
        Boolean isNotice, Long parentId, Long seq) {
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
