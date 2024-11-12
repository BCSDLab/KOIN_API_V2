package in.koreatech.koin.domain.shop.model.menu;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenuImage is a Querydsl query type for MenuImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuImage extends EntityPathBase<MenuImage> {

    private static final long serialVersionUID = 546422222L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenuImage menuImage = new QMenuImage("menuImage");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final QMenu menu;

    public QMenuImage(String variable) {
        this(MenuImage.class, forVariable(variable), INITS);
    }

    public QMenuImage(Path<? extends MenuImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenuImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenuImage(PathMetadata metadata, PathInits inits) {
        this(MenuImage.class, metadata, inits);
    }

    public QMenuImage(Class<? extends MenuImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.menu = inits.isInitialized("menu") ? new QMenu(forProperty("menu")) : null;
    }

}

