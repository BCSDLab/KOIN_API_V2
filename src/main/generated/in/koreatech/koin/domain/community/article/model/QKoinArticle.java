package in.koreatech.koin.domain.community.article.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QKoinArticle is a Querydsl query type for KoinArticle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QKoinArticle extends EntityPathBase<KoinArticle> {

    private static final long serialVersionUID = -1447094993L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QKoinArticle koinArticle = new QKoinArticle("koinArticle");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final QArticle article;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QKoinArticle(String variable) {
        this(KoinArticle.class, forVariable(variable), INITS);
    }

    public QKoinArticle(Path<? extends KoinArticle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QKoinArticle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QKoinArticle(PathMetadata metadata, PathInits inits) {
        this(KoinArticle.class, metadata, inits);
    }

    public QKoinArticle(Class<? extends KoinArticle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new QArticle(forProperty("article"), inits.get("article")) : null;
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

