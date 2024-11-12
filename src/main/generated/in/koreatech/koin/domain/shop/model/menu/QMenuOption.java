package in.koreatech.koin.domain.shop.model.menu;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenuOption is a Querydsl query type for MenuOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuOption extends EntityPathBase<MenuOption> {

    private static final long serialVersionUID = -65666462L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenuOption menuOption = new QMenuOption("menuOption");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final QMenu menu;

    public final StringPath option = createString("option");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMenuOption(String variable) {
        this(MenuOption.class, forVariable(variable), INITS);
    }

    public QMenuOption(Path<? extends MenuOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenuOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenuOption(PathMetadata metadata, PathInits inits) {
        this(MenuOption.class, metadata, inits);
    }

    public QMenuOption(Class<? extends MenuOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.menu = inits.isInitialized("menu") ? new QMenu(forProperty("menu")) : null;
    }

}

