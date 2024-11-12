package in.koreatech.koin.admin.abtest.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAbtestVariable is a Querydsl query type for AbtestVariable
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAbtestVariable extends EntityPathBase<AbtestVariable> {

    private static final long serialVersionUID = 1627602946L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAbtestVariable abtestVariable = new QAbtestVariable("abtestVariable");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final QAbtest abtest;

    public final ListPath<AccessHistoryAbtestVariable, QAccessHistoryAbtestVariable> accessHistoryAbtestVariables = this.<AccessHistoryAbtestVariable, QAccessHistoryAbtestVariable>createList("accessHistoryAbtestVariables", AccessHistoryAbtestVariable.class, QAccessHistoryAbtestVariable.class, PathInits.DIRECT2);

    public final NumberPath<Integer> count = createNumber("count", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath displayName = createString("displayName");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> rate = createNumber("rate", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAbtestVariable(String variable) {
        this(AbtestVariable.class, forVariable(variable), INITS);
    }

    public QAbtestVariable(Path<? extends AbtestVariable> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAbtestVariable(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAbtestVariable(PathMetadata metadata, PathInits inits) {
        this(AbtestVariable.class, metadata, inits);
    }

    public QAbtestVariable(Class<? extends AbtestVariable> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.abtest = inits.isInitialized("abtest") ? new QAbtest(forProperty("abtest"), inits.get("abtest")) : null;
    }

}

