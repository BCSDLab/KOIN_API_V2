package in.koreatech.koin.admin.abtest.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAccessHistory is a Querydsl query type for AccessHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAccessHistory extends EntityPathBase<AccessHistory> {

    private static final long serialVersionUID = 1136347133L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAccessHistory accessHistory = new QAccessHistory("accessHistory");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final ListPath<AccessHistoryAbtestVariable, QAccessHistoryAbtestVariable> accessHistoryAbtestVariables = this.<AccessHistoryAbtestVariable, QAccessHistoryAbtestVariable>createList("accessHistoryAbtestVariables", AccessHistoryAbtestVariable.class, QAccessHistoryAbtestVariable.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QDevice device;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> lastAccessedAt = createDateTime("lastAccessedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAccessHistory(String variable) {
        this(AccessHistory.class, forVariable(variable), INITS);
    }

    public QAccessHistory(Path<? extends AccessHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAccessHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAccessHistory(PathMetadata metadata, PathInits inits) {
        this(AccessHistory.class, metadata, inits);
    }

    public QAccessHistory(Class<? extends AccessHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.device = inits.isInitialized("device") ? new QDevice(forProperty("device"), inits.get("device")) : null;
    }

}

