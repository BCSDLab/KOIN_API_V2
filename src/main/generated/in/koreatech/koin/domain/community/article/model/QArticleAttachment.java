package in.koreatech.koin.domain.community.article.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArticleAttachment is a Querydsl query type for ArticleAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArticleAttachment extends EntityPathBase<ArticleAttachment> {

    private static final long serialVersionUID = 906025787L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QArticleAttachment articleAttachment = new QArticleAttachment("articleAttachment");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final QArticle article;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath hash = createString("hash");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath url = createString("url");

    public QArticleAttachment(String variable) {
        this(ArticleAttachment.class, forVariable(variable), INITS);
    }

    public QArticleAttachment(Path<? extends ArticleAttachment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QArticleAttachment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QArticleAttachment(PathMetadata metadata, PathInits inits) {
        this(ArticleAttachment.class, metadata, inits);
    }

    public QArticleAttachment(Class<? extends ArticleAttachment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new QArticle(forProperty("article"), inits.get("article")) : null;
    }

}

