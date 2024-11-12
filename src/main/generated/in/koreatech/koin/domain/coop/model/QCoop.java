package in.koreatech.koin.domain.coop.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCoop is a Querydsl query type for Coop
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoop extends EntityPathBase<Coop> {

    private static final long serialVersionUID = -1068926857L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCoop coop = new QCoop("coop");

    public final StringPath coopId = createString("coopId");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QCoop(String variable) {
        this(Coop.class, forVariable(variable), INITS);
    }

    public QCoop(Path<? extends Coop> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCoop(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCoop(PathMetadata metadata, PathInits inits) {
        this(Coop.class, metadata, inits);
    }

    public QCoop(Class<? extends Coop> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

