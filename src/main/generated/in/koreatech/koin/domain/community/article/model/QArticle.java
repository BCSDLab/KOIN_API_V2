package in.koreatech.koin.domain.community.article.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArticle is a Querydsl query type for Article
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArticle extends EntityPathBase<Article> {

    private static final long serialVersionUID = 930626552L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QArticle article = new QArticle("article");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final ListPath<ArticleAttachment, QArticleAttachment> attachments = this.<ArticleAttachment, QArticleAttachment>createList("attachments", ArticleAttachment.class, QArticleAttachment.class, PathInits.DIRECT2);

    public final QBoard board;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> hit = createNumber("hit", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final BooleanPath isNotice = createBoolean("isNotice");

    public final QKoinArticle koinArticle;

    public final QKoreatechArticle koreatechArticle;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QArticle(String variable) {
        this(Article.class, forVariable(variable), INITS);
    }

    public QArticle(Path<? extends Article> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QArticle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QArticle(PathMetadata metadata, PathInits inits) {
        this(Article.class, metadata, inits);
    }

    public QArticle(Class<? extends Article> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.board = inits.isInitialized("board") ? new QBoard(forProperty("board")) : null;
        this.koinArticle = inits.isInitialized("koinArticle") ? new QKoinArticle(forProperty("koinArticle"), inits.get("koinArticle")) : null;
        this.koreatechArticle = inits.isInitialized("koreatechArticle") ? new QKoreatechArticle(forProperty("koreatechArticle"), inits.get("koreatechArticle")) : null;
    }

}

