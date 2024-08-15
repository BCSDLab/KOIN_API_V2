package in.koreatech.koin.domain.shop.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopReviewMenu is a Querydsl query type for ShopReviewMenu
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShopReviewMenu extends EntityPathBase<ShopReviewMenu> {

    private static final long serialVersionUID = 539874574L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopReviewMenu shopReviewMenu = new QShopReviewMenu("shopReviewMenu");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath menuName = createString("menuName");

    public final QShopReview review;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QShopReviewMenu(String variable) {
        this(ShopReviewMenu.class, forVariable(variable), INITS);
    }

    public QShopReviewMenu(Path<? extends ShopReviewMenu> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopReviewMenu(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopReviewMenu(PathMetadata metadata, PathInits inits) {
        this(ShopReviewMenu.class, metadata, inits);
    }

    public QShopReviewMenu(Class<? extends ShopReviewMenu> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QShopReview(forProperty("review"), inits.get("review")) : null;
    }

}

