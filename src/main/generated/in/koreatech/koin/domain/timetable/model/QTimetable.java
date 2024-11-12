package in.koreatech.koin.domain.timetable.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTimetable is a Querydsl query type for Timetable
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTimetable extends EntityPathBase<Timetable> {

    private static final long serialVersionUID = -1125521197L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTimetable timetable = new QTimetable("timetable");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final StringPath classPlace = createString("classPlace");

    public final StringPath classTime = createString("classTime");

    public final StringPath classTitle = createString("classTitle");

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath department = createString("department");

    public final StringPath designScore = createString("designScore");

    public final StringPath grades = createString("grades");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath lectureClass = createString("lectureClass");

    public final StringPath memo = createString("memo");

    public final StringPath professor = createString("professor");

    public final StringPath regularNumber = createString("regularNumber");

    public final QSemester semester;

    public final StringPath target = createString("target");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QTimetable(String variable) {
        this(Timetable.class, forVariable(variable), INITS);
    }

    public QTimetable(Path<? extends Timetable> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTimetable(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTimetable(PathMetadata metadata, PathInits inits) {
        this(Timetable.class, metadata, inits);
    }

    public QTimetable(Class<? extends Timetable> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.semester = inits.isInitialized("semester") ? new QSemester(forProperty("semester")) : null;
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

