package in.koreatech.koin.domain.member.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTechStack is a Querydsl query type for TechStack
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTechStack extends EntityPathBase<TechStack> {

    private static final long serialVersionUID = 828872949L;

    public static final QTechStack techStack = new QTechStack("techStack");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> trackId = createNumber("trackId", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTechStack(String variable) {
        super(TechStack.class, forVariable(variable));
    }

    public QTechStack(Path<? extends TechStack> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTechStack(PathMetadata metadata) {
        super(TechStack.class, metadata);
    }

}

