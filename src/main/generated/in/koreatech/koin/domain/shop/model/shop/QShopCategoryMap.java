package in.koreatech.koin.domain.shop.model.shop;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopCategoryMap is a Querydsl query type for ShopCategoryMap
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShopCategoryMap extends EntityPathBase<ShopCategoryMap> {

    private static final long serialVersionUID = 971011921L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopCategoryMap shopCategoryMap = new QShopCategoryMap("shopCategoryMap");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final QShop shop;

    public final QShopCategory shopCategory;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QShopCategoryMap(String variable) {
        this(ShopCategoryMap.class, forVariable(variable), INITS);
    }

    public QShopCategoryMap(Path<? extends ShopCategoryMap> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopCategoryMap(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopCategoryMap(PathMetadata metadata, PathInits inits) {
        this(ShopCategoryMap.class, metadata, inits);
    }

    public QShopCategoryMap(Class<? extends ShopCategoryMap> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shop = inits.isInitialized("shop") ? new QShop(forProperty("shop"), inits.get("shop")) : null;
        this.shopCategory = inits.isInitialized("shopCategory") ? new QShopCategory(forProperty("shopCategory")) : null;
    }

}

