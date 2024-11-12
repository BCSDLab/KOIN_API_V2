package in.koreatech.koin.domain.benefit.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBenefitCategory is a Querydsl query type for BenefitCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBenefitCategory extends EntityPathBase<BenefitCategory> {

    private static final long serialVersionUID = 1913436381L;

    public static final QBenefitCategory benefitCategory = new QBenefitCategory("benefitCategory");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath detail = createString("detail");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath offImageUrl = createString("offImageUrl");

    public final StringPath onImageUrl = createString("onImageUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBenefitCategory(String variable) {
        super(BenefitCategory.class, forVariable(variable));
    }

    public QBenefitCategory(Path<? extends BenefitCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBenefitCategory(PathMetadata metadata) {
        super(BenefitCategory.class, metadata);
    }

}

