package in.koreatech.koin.domain.shop.model;

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

    private static final long serialVersionUID = 821296960L;

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

    public final NumberPath<Integer> shopId = createNumber("shopId", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMenu(String variable) {
        super(Menu.class, forVariable(variable));
    }

    public QMenu(Path<? extends Menu> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMenu(PathMetadata metadata) {
        super(Menu.class, metadata);
    }

}

