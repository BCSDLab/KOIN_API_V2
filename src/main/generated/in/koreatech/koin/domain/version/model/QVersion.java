package in.koreatech.koin.domain.version.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QVersion is a Querydsl query type for Version
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVersion extends EntityPathBase<Version> {

    private static final long serialVersionUID = -1185050239L;

    public static final QVersion version1 = new QVersion("version1");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isPrevious = createBoolean("isPrevious");

    public final StringPath title = createString("title");

    public final StringPath type = createString("type");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath version = createString("version");

    public QVersion(String variable) {
        super(Version.class, forVariable(variable));
    }

    public QVersion(Path<? extends Version> path) {
        super(path.getType(), path.getMetadata());
    }

    public QVersion(PathMetadata metadata) {
        super(Version.class, metadata);
    }

}

