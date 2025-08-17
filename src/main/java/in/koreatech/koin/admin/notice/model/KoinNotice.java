package in.koreatech.koin.admin.notice.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.lang.Boolean.*;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.community.article.model.Article;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "koin_notice")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class KoinNotice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @NotNull
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = FALSE;

    @Builder
    private KoinNotice(
        Integer id,
        Article article,
        Admin admin,
        Boolean isDeleted
    ) {
        this.id = id;
        this.article = article;
        this.admin = admin;
        this.isDeleted = isDeleted;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
