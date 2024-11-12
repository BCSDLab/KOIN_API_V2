package in.koreatech.koin.domain.coopshop.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCoopOpen is a Querydsl query type for CoopOpen
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoopOpen extends EntityPathBase<CoopOpen> {

    private static final long serialVersionUID = -1359239637L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCoopOpen coopOpen = new QCoopOpen("coopOpen");

    public final StringPath closeTime = createString("closeTime");

    public final QCoopShop coopShop;

    public final EnumPath<DayType> dayOfWeek = createEnum("dayOfWeek", DayType.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath openTime = createString("openTime");

    public final StringPath type = createString("type");

    public QCoopOpen(String variable) {
        this(CoopOpen.class, forVariable(variable), INITS);
    }

    public QCoopOpen(Path<? extends CoopOpen> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCoopOpen(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCoopOpen(PathMetadata metadata, PathInits inits) {
        this(CoopOpen.class, metadata, inits);
    }

    public QCoopOpen(Class<? extends CoopOpen> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coopShop = inits.isInitialized("coopShop") ? new QCoopShop(forProperty("coopShop"), inits.get("coopShop")) : null;
    }

}

