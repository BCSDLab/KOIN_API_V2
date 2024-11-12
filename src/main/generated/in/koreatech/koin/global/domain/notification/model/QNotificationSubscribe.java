package in.koreatech.koin.global.domain.notification.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotificationSubscribe is a Querydsl query type for NotificationSubscribe
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationSubscribe extends EntityPathBase<NotificationSubscribe> {

    private static final long serialVersionUID = -2015848476L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotificationSubscribe notificationSubscribe = new QNotificationSubscribe("notificationSubscribe");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<NotificationDetailSubscribeType> detailType = createEnum("detailType", NotificationDetailSubscribeType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<NotificationSubscribeType> subscribeType = createEnum("subscribeType", NotificationSubscribeType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QNotificationSubscribe(String variable) {
        this(NotificationSubscribe.class, forVariable(variable), INITS);
    }

    public QNotificationSubscribe(Path<? extends NotificationSubscribe> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotificationSubscribe(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotificationSubscribe(PathMetadata metadata, PathInits inits) {
        this(NotificationSubscribe.class, metadata, inits);
    }

    public QNotificationSubscribe(Class<? extends NotificationSubscribe> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

