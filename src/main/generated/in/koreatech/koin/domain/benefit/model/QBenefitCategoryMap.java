package in.koreatech.koin.domain.benefit.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBenefitCategoryMap is a Querydsl query type for BenefitCategoryMap
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBenefitCategoryMap extends EntityPathBase<BenefitCategoryMap> {

    private static final long serialVersionUID = 377350975L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBenefitCategoryMap benefitCategoryMap = new QBenefitCategoryMap("benefitCategoryMap");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final QBenefitCategory benefitCategory;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final in.koreatech.koin.domain.shop.model.shop.QShop shop;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBenefitCategoryMap(String variable) {
        this(BenefitCategoryMap.class, forVariable(variable), INITS);
    }

    public QBenefitCategoryMap(Path<? extends BenefitCategoryMap> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBenefitCategoryMap(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBenefitCategoryMap(PathMetadata metadata, PathInits inits) {
        this(BenefitCategoryMap.class, metadata, inits);
    }

    public QBenefitCategoryMap(Class<? extends BenefitCategoryMap> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.benefitCategory = inits.isInitialized("benefitCategory") ? new QBenefitCategory(forProperty("benefitCategory")) : null;
        this.shop = inits.isInitialized("shop") ? new in.koreatech.koin.domain.shop.model.shop.QShop(forProperty("shop"), inits.get("shop")) : null;
    }

}

