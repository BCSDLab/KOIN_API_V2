package in.koreatech.koin.domain.community.article.model;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import in.koreatech.koin._common.converter.HashAttributeConverter;
import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Where(clause = "is_deleted=0")
@Table(name = "article_attachments", uniqueConstraints = {
    @UniqueConstraint(name = "ux_article_attachment", columnNames = {"article_id", "hash"})
})
@NoArgsConstructor(access = PROTECTED)
public class ArticleAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(cascade = {PERSIST, MERGE, REMOVE})
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Basic(fetch = FetchType.LAZY)
    @Convert(converter = HashAttributeConverter.class)
    @Column(name = "hash", columnDefinition = "BINARY(32)", nullable = false)
    private String hash;

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
    private ArticleAttachment(Article article, String hash, String url, String name) {
        this.hash = hash;
        this.article = article;
        this.url = url;
        this.name = name;
    }
}
