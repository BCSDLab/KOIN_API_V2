package in.koreatech.koin.domain.shop.model.shop;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopOpen is a Querydsl query type for ShopOpen
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShopOpen extends EntityPathBase<ShopOpen> {

    private static final long serialVersionUID = 1275325911L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShopOpen shopOpen = new QShopOpen("shopOpen");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final BooleanPath closed = createBoolean("closed");

    public final TimePath<java.time.LocalTime> closeTime = createTime("closeTime", java.time.LocalTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath dayOfWeek = createString("dayOfWeek");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final TimePath<java.time.LocalTime> openTime = createTime("openTime", java.time.LocalTime.class);

    public final QShop shop;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QShopOpen(String variable) {
        this(ShopOpen.class, forVariable(variable), INITS);
    }

    public QShopOpen(Path<? extends ShopOpen> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShopOpen(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShopOpen(PathMetadata metadata, PathInits inits) {
        this(ShopOpen.class, metadata, inits);
    }

    public QShopOpen(Class<? extends ShopOpen> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shop = inits.isInitialized("shop") ? new QShop(forProperty("shop"), inits.get("shop")) : null;
    }

}

