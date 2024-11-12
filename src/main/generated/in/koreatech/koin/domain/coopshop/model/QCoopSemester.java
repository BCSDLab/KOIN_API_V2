package in.koreatech.koin.domain.coopshop.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCoopSemester is a Querydsl query type for CoopSemester
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoopSemester extends EntityPathBase<CoopSemester> {

    private static final long serialVersionUID = 1175763097L;

    public static final QCoopSemester coopSemester = new QCoopSemester("coopSemester");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final ListPath<CoopShop, QCoopShop> coopShops = this.<CoopShop, QCoopShop>createList("coopShops", CoopShop.class, QCoopShop.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DatePath<java.time.LocalDate> fromDate = createDate("fromDate", java.time.LocalDate.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final BooleanPath isApplied = createBoolean("isApplied");

    public final StringPath semester = createString("semester");

    public final DatePath<java.time.LocalDate> toDate = createDate("toDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCoopSemester(String variable) {
        super(CoopSemester.class, forVariable(variable));
    }

    public QCoopSemester(Path<? extends CoopSemester> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoopSemester(PathMetadata metadata) {
        super(CoopSemester.class, metadata);
    }

}

