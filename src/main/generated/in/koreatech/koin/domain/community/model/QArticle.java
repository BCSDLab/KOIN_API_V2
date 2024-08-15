package in.koreatech.koin.domain.community.model;

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

    private static final long serialVersionUID = -1854445072L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QArticle article = new QArticle("article");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final QBoard board;

    public final ListPath<Comment, QComment> comment = this.<Comment, QComment>createList("comment", Comment.class, QComment.class, PathInits.DIRECT2);

    public final NumberPath<Byte> commentCount = createNumber("commentCount", Byte.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> hit = createNumber("hit", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath ip = createString("ip");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final BooleanPath isNotice = createBoolean("isNotice");

    public final BooleanPath isSolved = createBoolean("isSolved");

    public final StringPath meta = createString("meta");

    public final StringPath nickname = createString("nickname");

    public final NumberPath<Integer> noticeArticleId = createNumber("noticeArticleId", Integer.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final in.koreatech.koin.domain.user.model.QUser user;

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
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

