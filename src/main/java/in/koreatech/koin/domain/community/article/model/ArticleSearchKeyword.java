package in.koreatech.koin.domain.community.article.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "article_search_keywords", indexes = {
    @Index(name = "idx_keyword", columnList = "keyword"),
    @Index(name = "idx_last_searched_at", columnList = "last_searched_at"),
})
@NoArgsConstructor(access = PROTECTED)
public class ArticleSearchKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String keyword;

    @Column(nullable = false)
    private double weight;

    @Column(name = "last_searched_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastSearchedAt;

    @Column(name = "total_searches", nullable = false)
    private int totalSearch;

    @Builder
    private ArticleSearchKeyword(String keyword, double weight, LocalDateTime lastSearchedAt, int totalSearch) {
        this.keyword = keyword;
        this.weight = weight;
        this.lastSearchedAt = lastSearchedAt;
        this.totalSearch = totalSearch;
    }

    public void updateWeight(double newWeight) {
        this.weight = newWeight;
        this.lastSearchedAt = LocalDateTime.now();
        this.totalSearch++;
    }

    public void resetWeight() {
        this.weight = 1.0;
    }

    public void incrementTotalSearch() {
        this.totalSearch++;
    }

    public void updateLastSearchedAt(LocalDateTime now) {
        this.lastSearchedAt = now;
    }

    public void increaseTotalSearchBy(int count) {
        this.totalSearch += count;
    }
}
