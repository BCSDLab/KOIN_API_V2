package in.koreatech.koin.domain.coopshop.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCoopShop is a Querydsl query type for CoopShop
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoopShop extends EntityPathBase<CoopShop> {

    private static final long serialVersionUID = -1359127849L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCoopShop coopShop = new QCoopShop("coopShop");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final ListPath<CoopOpen, QCoopOpen> coopOpens = this.<CoopOpen, QCoopOpen>createList("coopOpens", CoopOpen.class, QCoopOpen.class, PathInits.DIRECT2);

    public final QCoopSemester coopSemester;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath location = createString("location");

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public final StringPath remarks = createString("remarks");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCoopShop(String variable) {
        this(CoopShop.class, forVariable(variable), INITS);
    }

    public QCoopShop(Path<? extends CoopShop> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCoopShop(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCoopShop(PathMetadata metadata, PathInits inits) {
        this(CoopShop.class, metadata, inits);
    }

    public QCoopShop(Class<? extends CoopShop> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.coopSemester = inits.isInitialized("coopSemester") ? new QCoopSemester(forProperty("coopSemester")) : null;
    }

}

