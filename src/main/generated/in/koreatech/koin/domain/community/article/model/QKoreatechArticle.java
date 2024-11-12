package in.koreatech.koin.domain.community.article.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QKoreatechArticle is a Querydsl query type for KoreatechArticle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QKoreatechArticle extends EntityPathBase<KoreatechArticle> {

    private static final long serialVersionUID = -513977900L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QKoreatechArticle koreatechArticle = new QKoreatechArticle("koreatechArticle");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final QArticle article;

    public final StringPath author = createString("author");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final NumberPath<Integer> portalHit = createNumber("portalHit", Integer.class);

    public final NumberPath<Integer> portalNum = createNumber("portalNum", Integer.class);

    public final DatePath<java.time.LocalDate> registeredAt = createDate("registeredAt", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath url = createString("url");

    public QKoreatechArticle(String variable) {
        this(KoreatechArticle.class, forVariable(variable), INITS);
    }

    public QKoreatechArticle(Path<? extends KoreatechArticle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QKoreatechArticle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QKoreatechArticle(PathMetadata metadata, PathInits inits) {
        this(KoreatechArticle.class, metadata, inits);
    }

    public QKoreatechArticle(Class<? extends KoreatechArticle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.article = inits.isInitialized("article") ? new QArticle(forProperty("article"), inits.get("article")) : null;
    }

}

