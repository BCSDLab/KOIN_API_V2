package in.koreatech.koin.domain.timetable.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLecture is a Querydsl query type for Lecture
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLecture extends EntityPathBase<Lecture> {

    private static final long serialVersionUID = 972134832L;

    public static final QLecture lecture = new QLecture("lecture");

    public final StringPath classTime = createString("classTime");

    public final StringPath code = createString("code");

    public final StringPath department = createString("department");

    public final StringPath designScore = createString("designScore");

    public final StringPath grades = createString("grades");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath isElearning = createString("isElearning");

    public final StringPath isEnglish = createString("isEnglish");

    public final StringPath lectureClass = createString("lectureClass");

    public final StringPath name = createString("name");

    public final StringPath professor = createString("professor");

    public final StringPath regularNumber = createString("regularNumber");

    public final StringPath semester = createString("semester");

    public final StringPath target = createString("target");

    public QLecture(String variable) {
        super(Lecture.class, forVariable(variable));
    }

    public QLecture(Path<? extends Lecture> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLecture(PathMetadata metadata) {
        super(Lecture.class, metadata);
    }

}

