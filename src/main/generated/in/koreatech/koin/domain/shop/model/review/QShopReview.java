package in.koreatech.koin.domain.shop.model.review;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopReview is a Querydsl query type for ShopReview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShopReview extends EntityPathBase<ShopReview> {

    private static final long serialVersionUID = 356668995L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopReview shopReview = new QShopReview("shopReview");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final ListPath<ShopReviewImage, QShopReviewImage> images = this.<ShopReviewImage, QShopReviewImage>createList("images", ShopReviewImage.class, QShopReviewImage.class, PathInits.DIRECT2);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final ListPath<ShopReviewMenu, QShopReviewMenu> menus = this.<ShopReviewMenu, QShopReviewMenu>createList("menus", ShopReviewMenu.class, QShopReviewMenu.class, PathInits.DIRECT2);

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final ListPath<ShopReviewReport, QShopReviewReport> reports = this.<ShopReviewReport, QShopReviewReport>createList("reports", ShopReviewReport.class, QShopReviewReport.class, PathInits.DIRECT2);

    public final in.koreatech.koin.domain.student.model.QStudent reviewer;

    public final in.koreatech.koin.domain.shop.model.shop.QShop shop;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QShopReview(String variable) {
        this(ShopReview.class, forVariable(variable), INITS);
    }

    public QShopReview(Path<? extends ShopReview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopReview(PathMetadata metadata, PathInits inits) {
        this(ShopReview.class, metadata, inits);
    }

    public QShopReview(Class<? extends ShopReview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reviewer = inits.isInitialized("reviewer") ? new in.koreatech.koin.domain.student.model.QStudent(forProperty("reviewer"), inits.get("reviewer")) : null;
        this.shop = inits.isInitialized("shop") ? new in.koreatech.koin.domain.shop.model.shop.QShop(forProperty("shop"), inits.get("shop")) : null;
    }

}

