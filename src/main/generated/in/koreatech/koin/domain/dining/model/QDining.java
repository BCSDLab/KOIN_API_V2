package in.koreatech.koin.domain.dining.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDining is a Querydsl query type for Dining
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDining extends EntityPathBase<Dining> {

    private static final long serialVersionUID = -1577612169L;

    public static final QDining dining = new QDining("dining");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final DateTimePath<java.time.LocalDateTime> isChanged = createDateTime("isChanged", java.time.LocalDateTime.class);

    public final NumberPath<Integer> kcal = createNumber("kcal", Integer.class);

    public final NumberPath<Integer> likes = createNumber("likes", Integer.class);

    public final StringPath menu = createString("menu");

    public final StringPath place = createString("place");

    public final NumberPath<Integer> priceCard = createNumber("priceCard", Integer.class);

    public final NumberPath<Integer> priceCash = createNumber("priceCash", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> soldOut = createDateTime("soldOut", java.time.LocalDateTime.class);

    public final EnumPath<DiningType> type = createEnum("type", DiningType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDining(String variable) {
        super(Dining.class, forVariable(variable));
    }

    public QDining(Path<? extends Dining> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDining(PathMetadata metadata) {
        super(Dining.class, metadata);
    }

}

