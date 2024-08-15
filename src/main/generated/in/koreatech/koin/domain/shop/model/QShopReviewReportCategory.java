package in.koreatech.koin.domain.shop.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QShopReviewReportCategory is a Querydsl query type for ShopReviewReportCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShopReviewReportCategory extends EntityPathBase<ShopReviewReportCategory> {

    private static final long serialVersionUID = -216452287L;

    public static final QShopReviewReportCategory shopReviewReportCategory = new QShopReviewReportCategory("shopReviewReportCategory");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath detail = createString("detail");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QShopReviewReportCategory(String variable) {
        super(ShopReviewReportCategory.class, forVariable(variable));
    }

    public QShopReviewReportCategory(Path<? extends ShopReviewReportCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QShopReviewReportCategory(PathMetadata metadata) {
        super(ShopReviewReportCategory.class, metadata);
    }

}

