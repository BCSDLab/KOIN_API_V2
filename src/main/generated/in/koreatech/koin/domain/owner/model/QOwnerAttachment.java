package in.koreatech.koin.domain.owner.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOwnerAttachment is a Querydsl query type for OwnerAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOwnerAttachment extends EntityPathBase<OwnerAttachment> {

    private static final long serialVersionUID = -372209542L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOwnerAttachment ownerAttachment = new QOwnerAttachment("ownerAttachment");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final QOwner owner;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath url = createString("url");

    public QOwnerAttachment(String variable) {
        this(OwnerAttachment.class, forVariable(variable), INITS);
    }

    public QOwnerAttachment(Path<? extends OwnerAttachment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOwnerAttachment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOwnerAttachment(PathMetadata metadata, PathInits inits) {
        this(OwnerAttachment.class, metadata, inits);
    }

    public QOwnerAttachment(Class<? extends OwnerAttachment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new QOwner(forProperty("owner"), inits.get("owner")) : null;
    }

}

