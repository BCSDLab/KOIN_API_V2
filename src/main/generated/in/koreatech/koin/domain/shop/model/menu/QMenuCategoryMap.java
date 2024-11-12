package in.koreatech.koin.domain.shop.model.menu;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenuCategoryMap is a Querydsl query type for MenuCategoryMap
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuCategoryMap extends EntityPathBase<MenuCategoryMap> {

    private static final long serialVersionUID = -1458759247L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenuCategoryMap menuCategoryMap = new QMenuCategoryMap("menuCategoryMap");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final QMenu menu;

    public final QMenuCategory menuCategory;

    public QMenuCategoryMap(String variable) {
        this(MenuCategoryMap.class, forVariable(variable), INITS);
    }

    public QMenuCategoryMap(Path<? extends MenuCategoryMap> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenuCategoryMap(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenuCategoryMap(PathMetadata metadata, PathInits inits) {
        this(MenuCategoryMap.class, metadata, inits);
    }

    public QMenuCategoryMap(Class<? extends MenuCategoryMap> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.menu = inits.isInitialized("menu") ? new QMenu(forProperty("menu")) : null;
        this.menuCategory = inits.isInitialized("menuCategory") ? new QMenuCategory(forProperty("menuCategory"), inits.get("menuCategory")) : null;
    }

}

