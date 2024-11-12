package in.koreatech.koin.domain.shop.model.review;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopReviewImage is a Querydsl query type for ShopReviewImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShopReviewImage extends EntityPathBase<ShopReviewImage> {

    private static final long serialVersionUID = 458216664L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopReviewImage shopReviewImage = new QShopReviewImage("shopReviewImage");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath imageUrls = createString("imageUrls");

    public final QShopReview review;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QShopReviewImage(String variable) {
        this(ShopReviewImage.class, forVariable(variable), INITS);
    }

    public QShopReviewImage(Path<? extends ShopReviewImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopReviewImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopReviewImage(PathMetadata metadata, PathInits inits) {
        this(ShopReviewImage.class, metadata, inits);
    }

    public QShopReviewImage(Class<? extends ShopReviewImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QShopReview(forProperty("review"), inits.get("review")) : null;
    }

}

