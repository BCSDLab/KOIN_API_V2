package in.koreatech.koin.domain.shop.model.menu;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenuCategory is a Querydsl query type for MenuCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuCategory extends EntityPathBase<MenuCategory> {

    private static final long serialVersionUID = 1448570795L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenuCategory menuCategory = new QMenuCategory("menuCategory");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final ListPath<MenuCategoryMap, QMenuCategoryMap> menuCategoryMaps = this.<MenuCategoryMap, QMenuCategoryMap>createList("menuCategoryMaps", MenuCategoryMap.class, QMenuCategoryMap.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final in.koreatech.koin.domain.shop.model.shop.QShop shop;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMenuCategory(String variable) {
        this(MenuCategory.class, forVariable(variable), INITS);
    }

    public QMenuCategory(Path<MenuCategory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenuCategory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenuCategory(PathMetadata metadata, PathInits inits) {
        this(MenuCategory.class, metadata, inits);
    }

    public QMenuCategory(Class<? extends MenuCategory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shop = inits.isInitialized("shop") ? new in.koreatech.koin.domain.shop.model.shop.QShop(forProperty("shop"), inits.get("shop")) : null;
    }

}

