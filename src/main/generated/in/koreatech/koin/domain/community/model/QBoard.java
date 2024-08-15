package in.koreatech.koin.domain.community.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBoard is a Querydsl query type for Board
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoard extends EntityPathBase<Board> {

    private static final long serialVersionUID = -103906720L;

    public static final QBoard board = new QBoard("board");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final NumberPath<Integer> articleCount = createNumber("articleCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isAnonymous = createBoolean("isAnonymous");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final BooleanPath isNotice = createBoolean("isNotice");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> parentId = createNumber("parentId", Integer.class);

    public final NumberPath<Integer> seq = createNumber("seq", Integer.class);

    public final StringPath tag = createString("tag");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBoard(String variable) {
        super(Board.class, forVariable(variable));
    }

    public QBoard(Path<? extends Board> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBoard(PathMetadata metadata) {
        super(Board.class, metadata);
    }

}

