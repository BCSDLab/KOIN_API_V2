package in.koreatech.koin.domain.member.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTrack is a Querydsl query type for Track
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTrack extends EntityPathBase<Track> {

    private static final long serialVersionUID = 964752942L;

    public static final QTrack track = new QTrack("track");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> headcount = createNumber("headcount", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTrack(String variable) {
        super(Track.class, forVariable(variable));
    }

    public QTrack(Path<? extends Track> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTrack(PathMetadata metadata) {
        super(Track.class, metadata);
    }

}

