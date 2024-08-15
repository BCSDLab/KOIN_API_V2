package in.koreatech.koin.domain.dining.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDiningLikes is a Querydsl query type for DiningLikes
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDiningLikes extends EntityPathBase<DiningLikes> {

    private static final long serialVersionUID = -1236207227L;

    public static final QDiningLikes diningLikes = new QDiningLikes("diningLikes");

    public final NumberPath<Integer> diningId = createNumber("diningId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public QDiningLikes(String variable) {
        super(DiningLikes.class, forVariable(variable));
    }

    public QDiningLikes(Path<? extends DiningLikes> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDiningLikes(PathMetadata metadata) {
        super(DiningLikes.class, metadata);
    }

}

