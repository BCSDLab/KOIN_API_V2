package in.koreatech.koin.domain.community.keyword.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Where(clause = "is_deleted = 0")
@Table(name = "article_keywords")
@NoArgsConstructor(access = PROTECTED)
public class ArticleKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String keyword;

    @Column(name = "last_used_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUsedAt;

    @Column(name = "is_filtered", nullable = false)
    private Boolean isFiltered = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "articleKeyword", cascade = CascadeType.PERSIST)
    private List<ArticleKeywordUserMap> articleKeywordUserMaps = new ArrayList<>();

    @Builder
    private ArticleKeyword(String keyword, LocalDateTime lastUsedAt, Boolean isFiltered) {
        this.keyword = keyword;
        this.lastUsedAt = lastUsedAt;
        this.isFiltered = isFiltered != null ? isFiltered : false;
    }

    public void addUserMap(ArticleKeywordUserMap keywordUserMap) {
        articleKeywordUserMaps.add(keywordUserMap);
        updateLastUsedAt();
    }

    private void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void applyFiltered(Boolean isFiltered) {
        this.isFiltered = isFiltered;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
        this.lastUsedAt = LocalDateTime.now();
    }
}
