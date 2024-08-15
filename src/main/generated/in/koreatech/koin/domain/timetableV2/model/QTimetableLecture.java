package in.koreatech.koin.domain.timetableV2.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTimetableLecture is a Querydsl query type for TimetableLecture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTimetableLecture extends EntityPathBase<TimetableLecture> {

    private static final long serialVersionUID = -2103024561L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTimetableLecture timetableLecture = new QTimetableLecture("timetableLecture");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final StringPath classPlace = createString("classPlace");

    public final StringPath classTime = createString("classTime");

    public final StringPath classTitle = createString("classTitle");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath grades = createString("grades");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final in.koreatech.koin.domain.timetable.model.QLecture lecture;

    public final StringPath memo = createString("memo");

    public final StringPath professor = createString("professor");

    public final QTimetableFrame timetableFrame;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTimetableLecture(String variable) {
        this(TimetableLecture.class, forVariable(variable), INITS);
    }

    public QTimetableLecture(Path<? extends TimetableLecture> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTimetableLecture(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTimetableLecture(PathMetadata metadata, PathInits inits) {
        this(TimetableLecture.class, metadata, inits);
    }

    public QTimetableLecture(Class<? extends TimetableLecture> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.lecture = inits.isInitialized("lecture") ? new in.koreatech.koin.domain.timetable.model.QLecture(forProperty("lecture")) : null;
        this.timetableFrame = inits.isInitialized("timetableFrame") ? new QTimetableFrame(forProperty("timetableFrame"), inits.get("timetableFrame")) : null;
    }

}

