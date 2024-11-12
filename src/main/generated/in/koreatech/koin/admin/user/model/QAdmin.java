package in.koreatech.koin.admin.user.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdmin is a Querydsl query type for Admin
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdmin extends EntityPathBase<Admin> {

    private static final long serialVersionUID = -2055366508L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdmin admin = new QAdmin("admin");

    public final BooleanPath canCreateAdmin = createBoolean("canCreateAdmin");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath superAdmin = createBoolean("superAdmin");

    public final EnumPath<in.koreatech.koin.admin.user.enums.TeamType> teamType = createEnum("teamType", in.koreatech.koin.admin.user.enums.TeamType.class);

    public final EnumPath<in.koreatech.koin.admin.user.enums.TrackType> trackType = createEnum("trackType", in.koreatech.koin.admin.user.enums.TrackType.class);

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QAdmin(String variable) {
        this(Admin.class, forVariable(variable), INITS);
    }

    public QAdmin(Path<? extends Admin> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdmin(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdmin(PathMetadata metadata, PathInits inits) {
        this(Admin.class, metadata, inits);
    }

    public QAdmin(Class<? extends Admin> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

