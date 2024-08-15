package in.koreatech.koin.domain.timetable.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSemester is a Querydsl query type for Semester
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSemester extends EntityPathBase<Semester> {

    private static final long serialVersionUID = -341439674L;

    public static final QSemester semester1 = new QSemester("semester1");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath semester = createString("semester");

    public QSemester(String variable) {
        super(Semester.class, forVariable(variable));
    }

    public QSemester(Path<? extends Semester> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSemester(PathMetadata metadata) {
        super(Semester.class, metadata);
    }

}

