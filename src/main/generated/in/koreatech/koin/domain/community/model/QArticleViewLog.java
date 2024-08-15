package in.koreatech.koin.domain.community.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArticleViewLog is a Querydsl query type for ArticleViewLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArticleViewLog extends EntityPathBase<ArticleViewLog> {

    private static final long serialVersionUID = 1735774799L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QArticleViewLog articleViewLog = new QArticleViewLog("articleViewLog");

    public final QArticle article;

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath ip = createString("ip");

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QArticleViewLog(String variable) {
        this(ArticleViewLog.class, forVariable(variable), INITS);
    }

    public QArticleViewLog(Path<? extends ArticleViewLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QArticleViewLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QArticleViewLog(PathMetadata metadata, PathInits inits) {
        this(ArticleViewLog.class, metadata, inits);
    }

    public QArticleViewLog(Class<? extends ArticleViewLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new QArticle(forProperty("article"), inits.get("article")) : null;
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

