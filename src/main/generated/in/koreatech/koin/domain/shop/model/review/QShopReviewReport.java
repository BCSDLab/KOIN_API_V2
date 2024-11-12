package in.koreatech.koin.domain.shop.model.review;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopReviewReport is a Querydsl query type for ShopReviewReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShopReviewReport extends EntityPathBase<ShopReviewReport> {

    private static final long serialVersionUID = 1570543959L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopReviewReport shopReviewReport = new QShopReviewReport("shopReviewReport");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final EnumPath<ReportStatus> reportStatus = createEnum("reportStatus", ReportStatus.class);

    public final QShopReview review;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final in.koreatech.koin.domain.student.model.QStudent userId;

    public QShopReviewReport(String variable) {
        this(ShopReviewReport.class, forVariable(variable), INITS);
    }

    public QShopReviewReport(Path<? extends ShopReviewReport> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopReviewReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopReviewReport(PathMetadata metadata, PathInits inits) {
        this(ShopReviewReport.class, metadata, inits);
    }

    public QShopReviewReport(Class<? extends ShopReviewReport> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QShopReview(forProperty("review"), inits.get("review")) : null;
        this.userId = inits.isInitialized("userId") ? new in.koreatech.koin.domain.student.model.QStudent(forProperty("userId"), inits.get("userId")) : null;
    }

}

