package in.koreatech.koin.admin.abtest.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAccessHistoryAbtestVariable is a Querydsl query type for AccessHistoryAbtestVariable
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAccessHistoryAbtestVariable extends EntityPathBase<AccessHistoryAbtestVariable> {

    private static final long serialVersionUID = -799243988L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAccessHistoryAbtestVariable accessHistoryAbtestVariable = new QAccessHistoryAbtestVariable("accessHistoryAbtestVariable");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final QAccessHistory accessHistory;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QAbtestVariable variable;

    public QAccessHistoryAbtestVariable(String variable) {
        this(AccessHistoryAbtestVariable.class, forVariable(variable), INITS);
    }

    public QAccessHistoryAbtestVariable(Path<? extends AccessHistoryAbtestVariable> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAccessHistoryAbtestVariable(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAccessHistoryAbtestVariable(PathMetadata metadata, PathInits inits) {
        this(AccessHistoryAbtestVariable.class, metadata, inits);
    }

    public QAccessHistoryAbtestVariable(Class<? extends AccessHistoryAbtestVariable> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.accessHistory = inits.isInitialized("accessHistory") ? new QAccessHistory(forProperty("accessHistory"), inits.get("accessHistory")) : null;
        this.variable = inits.isInitialized("variable") ? new QAbtestVariable(forProperty("variable"), inits.get("variable")) : null;
    }

}

