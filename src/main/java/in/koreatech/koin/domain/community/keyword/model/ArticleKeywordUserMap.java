package in.koreatech.koin.domain.community.keyword.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "article_keyword_user_map", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"keyword_id", "user_id"})
})
@NoArgsConstructor(access = PROTECTED)
public class ArticleKeywordUserMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "keyword_id", nullable = false)
    private ArticleKeyword articleKeyword;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    private ArticleKeywordUserMap(ArticleKeyword articleKeyword, User user) {
        this.articleKeyword = articleKeyword;
        this.user = user;
    }
}
