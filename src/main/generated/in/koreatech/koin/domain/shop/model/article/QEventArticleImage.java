package in.koreatech.koin.domain.shop.model.article;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventArticleImage is a Querydsl query type for EventArticleImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventArticleImage extends EntityPathBase<EventArticleImage> {

    private static final long serialVersionUID = -1540407418L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventArticleImage eventArticleImage = new QEventArticleImage("eventArticleImage");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QEventArticle eventArticle;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath thumbnailImage = createString("thumbnailImage");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEventArticleImage(String variable) {
        this(EventArticleImage.class, forVariable(variable), INITS);
    }

    public QEventArticleImage(Path<? extends EventArticleImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventArticleImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventArticleImage(PathMetadata metadata, PathInits inits) {
        this(EventArticleImage.class, metadata, inits);
    }

    public QEventArticleImage(Class<? extends EventArticleImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.eventArticle = inits.isInitialized("eventArticle") ? new QEventArticle(forProperty("eventArticle"), inits.get("eventArticle")) : null;
    }

}

