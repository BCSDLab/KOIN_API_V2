package in.koreatech.koin.domain.shop.model.menu;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenu is a Querydsl query type for Menu
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenu extends EntityPathBase<Menu> {

    private static final long serialVersionUID = -1960563827L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenu menu = new QMenu("menu");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isHidden = createBoolean("isHidden");

    public final ListPath<MenuCategoryMap, QMenuCategoryMap> menuCategoryMaps = this.<MenuCategoryMap, QMenuCategoryMap>createList("menuCategoryMaps", MenuCategoryMap.class, QMenuCategoryMap.class, PathInits.DIRECT2);

    public final ListPath<MenuImage, QMenuImage> menuImages = this.<MenuImage, QMenuImage>createList("menuImages", MenuImage.class, QMenuImage.class, PathInits.DIRECT2);

    public final ListPath<MenuOption, QMenuOption> menuOptions = this.<MenuOption, QMenuOption>createList("menuOptions", MenuOption.class, QMenuOption.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final in.koreatech.koin.domain.shop.model.shop.QShop shop;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMenu(String variable) {
        this(Menu.class, forVariable(variable), INITS);
    }

    public QMenu(Path<? extends Menu> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenu(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenu(PathMetadata metadata, PathInits inits) {
        this(Menu.class, metadata, inits);
    }

    public QMenu(Class<? extends Menu> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.shop = inits.isInitialized("shop") ? new in.koreatech.koin.domain.shop.model.shop.QShop(forProperty("shop"), inits.get("shop")) : null;
    }

}

