package in.koreatech.koin.domain.owner.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOwner is a Querydsl query type for Owner
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOwner extends EntityPathBase<Owner> {

    private static final long serialVersionUID = -1541104649L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOwner owner = new QOwner("owner");

    public final StringPath account = createString("account");

    public final ListPath<OwnerAttachment, QOwnerAttachment> attachments = this.<OwnerAttachment, QOwnerAttachment>createList("attachments", OwnerAttachment.class, QOwnerAttachment.class, PathInits.DIRECT2);

    public final StringPath companyRegistrationNumber = createString("companyRegistrationNumber");

    public final BooleanPath grantEvent = createBoolean("grantEvent");

    public final BooleanPath grantShop = createBoolean("grantShop");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QOwner(String variable) {
        this(Owner.class, forVariable(variable), INITS);
    }

    public QOwner(Path<? extends Owner> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOwner(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOwner(PathMetadata metadata, PathInits inits) {
        this(Owner.class, metadata, inits);
    }

    public QOwner(Class<? extends Owner> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

