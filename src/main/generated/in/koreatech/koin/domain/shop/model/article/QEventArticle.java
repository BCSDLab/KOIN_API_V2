package in.koreatech.koin.domain.shop.model.article;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventArticle is a Querydsl query type for EventArticle
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventArticle extends EntityPathBase<EventArticle> {

    private static final long serialVersionUID = -677742891L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventArticle eventArticle = new QEventArticle("eventArticle");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final BooleanPath commentCount = createBoolean("commentCount");

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final StringPath eventTitle = createString("eventTitle");

    public final NumberPath<Integer> hit = createNumber("hit", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath ip = createString("ip");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath nickname = createString("nickname");

    public final in.koreatech.koin.domain.shop.model.shop.QShop shop;

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final StringPath thumbnail = createString("thumbnail");

    public final ListPath<EventArticleImage, QEventArticleImage> thumbnailImages = this.<EventArticleImage, QEventArticleImage>createList("thumbnailImages", EventArticleImage.class, QEventArticleImage.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QEventArticle(String variable) {
        this(EventArticle.class, forVariable(variable), INITS);
    }

    public QEventArticle(Path<? extends EventArticle> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventArticle(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventArticle(PathMetadata metadata, PathInits inits) {
        this(EventArticle.class, metadata, inits);
    }

    public QEventArticle(Class<? extends EventArticle> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shop = inits.isInitialized("shop") ? new in.koreatech.koin.domain.shop.model.shop.QShop(forProperty("shop"), inits.get("shop")) : null;
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

