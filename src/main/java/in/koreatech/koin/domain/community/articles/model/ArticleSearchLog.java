package in.koreatech.koin.domain.community.articles.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.global.domain.BaseEntity;
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
@Table(name = "article_search_logs", indexes = {
    @Index(name = "idx_keyword", columnList = "keyword"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@NoArgsConstructor(access = PROTECTED)
public class ArticleSearchLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private String ipAddress;

    @Builder
    public ArticleSearchLog(Integer id, String keyword, String ipAddress) {
        this.id = id;
        this.keyword = keyword;
        this.ipAddress = ipAddress;
    }
}
