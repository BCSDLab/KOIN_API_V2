package in.koreatech.koin.domain.timetableV2.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTimetableFrame is a Querydsl query type for TimetableFrame
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTimetableFrame extends EntityPathBase<TimetableFrame> {

    private static final long serialVersionUID = -588349314L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTimetableFrame timetableFrame = new QTimetableFrame("timetableFrame");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final BooleanPath isMain = createBoolean("isMain");

    public final StringPath name = createString("name");

    public final in.koreatech.koin.domain.timetable.model.QSemester semester;

    public final ListPath<TimetableLecture, QTimetableLecture> timetableLectures = this.<TimetableLecture, QTimetableLecture>createList("timetableLectures", TimetableLecture.class, QTimetableLecture.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QTimetableFrame(String variable) {
        this(TimetableFrame.class, forVariable(variable), INITS);
    }

    public QTimetableFrame(Path<? extends TimetableFrame> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTimetableFrame(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTimetableFrame(PathMetadata metadata, PathInits inits) {
        this(TimetableFrame.class, metadata, inits);
    }

    public QTimetableFrame(Class<? extends TimetableFrame> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.semester = inits.isInitialized("semester") ? new in.koreatech.koin.domain.timetable.model.QSemester(forProperty("semester")) : null;
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

