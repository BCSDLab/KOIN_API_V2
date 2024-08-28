package in.koreatech.koin.domain.community.model;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Where(clause = "is_deleted=0")
@Table(name = "article_attachments")
@NoArgsConstructor(access = PROTECTED)
public class ArticleAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(cascade = {PERSIST, MERGE, REMOVE})
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @NotNull
    @Column(name = "url", nullable = false)
    private String url;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private ArticleAttachment(Article article, String url, String name) {
        this.article = article;
        this.url = url;
        this.name = name;
    }
}
