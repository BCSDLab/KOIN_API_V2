package in.koreatech.koin.admin.abtest.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAbtest is a Querydsl query type for Abtest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAbtest extends EntityPathBase<Abtest> {

    private static final long serialVersionUID = -1130442906L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAbtest abtest = new QAbtest("abtest");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final ListPath<AbtestVariable, QAbtestVariable> abtestVariables = this.<AbtestVariable, QAbtestVariable>createList("abtestVariables", AbtestVariable.class, QAbtestVariable.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath creator = createString("creator");

    public final StringPath description = createString("description");

    public final StringPath displayTitle = createString("displayTitle");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final EnumPath<AbtestStatus> status = createEnum("status", AbtestStatus.class);

    public final StringPath team = createString("team");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QAbtestVariable winner;

    public QAbtest(String variable) {
        this(Abtest.class, forVariable(variable), INITS);
    }

    public QAbtest(Path<? extends Abtest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAbtest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAbtest(PathMetadata metadata, PathInits inits) {
        this(Abtest.class, metadata, inits);
    }

    public QAbtest(Class<? extends Abtest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.winner = inits.isInitialized("winner") ? new QAbtestVariable(forProperty("winner"), inits.get("winner")) : null;
    }

}

