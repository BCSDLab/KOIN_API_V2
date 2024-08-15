package in.koreatech.koin.domain.community.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.domain.community.exception.KeywordDuplicationException;
import in.koreatech.koin.global.domain.BaseEntity;
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

    @OneToMany(mappedBy = "articleKeyword", cascade = CascadeType.PERSIST)
    private List<ArticleKeywordUserMap> articleKeywordUserMaps = new ArrayList<>();

    @Builder
    public ArticleKeyword(String keyword, LocalDateTime lastUsedAt) {
        this.keyword = keyword;
        this.lastUsedAt = lastUsedAt;
    }

    public void addUserMap(ArticleKeywordUserMap keywordUserMap) {
        if (containsKeyword(keywordUserMap.getArticleKeyword().getKeyword())) {
            throw new KeywordDuplicationException("키워드는 중복될 수 없습니다.");
        }
        articleKeywordUserMaps.add(keywordUserMap);
    }

    private boolean containsKeyword(String keyword) {
        return articleKeywordUserMaps.stream()
            .anyMatch(map -> map.getArticleKeyword().getKeyword().equals(keyword));
    }
}
