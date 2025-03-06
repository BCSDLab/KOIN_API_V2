package in.koreatech.koin.domain.community.article.model;

import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Where(clause = "is_deleted=0")
@Table(name = "new_koin_articles", schema = "koin")
@NoArgsConstructor(access = PROTECTED)
public class KoinArticle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private KoinArticle(
        Integer id,
        Article article,
        User user,
        Boolean isDeleted
    ) {
        this.id = id;
        this.article = article;
        this.user = user;
        this.isDeleted = isDeleted;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
