package in.koreatech.koin.domain.community.article.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "article_search_keyword_ip_map", indexes = {
    @Index(name = "idx_ip_address", columnList = "ipAddress")}, uniqueConstraints = {
    @UniqueConstraint(name = "ux_keyword_ip", columnNames = {"keyword_id", "ipAddress"})
})
@NoArgsConstructor(access = PROTECTED)
public class ArticleSearchKeywordIpMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "keyword_id", nullable = false)
    private ArticleSearchKeyword articleSearchKeyword;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private Integer searchCount;

    @Builder
    private ArticleSearchKeywordIpMap(ArticleSearchKeyword articleSearchKeyword, String ipAddress, Integer searchCount) {
        this.articleSearchKeyword = articleSearchKeyword;
        this.ipAddress = ipAddress;
        this.searchCount = searchCount;
    }

    public void incrementSearchCount() {
        this.searchCount++;
    }

    public void resetSearchCount() {
        this.searchCount = 0;
    }
}
