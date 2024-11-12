package in.koreatech.koin.domain.shop.model.shop;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShopCategory is a Querydsl query type for ShopCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShopCategory extends EntityPathBase<ShopCategory> {

    private static final long serialVersionUID = -883296757L;

    public static final QShopCategory shopCategory = new QShopCategory("shopCategory");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath name = createString("name");

    public final ListPath<ShopCategoryMap, QShopCategoryMap> shopCategoryMaps = this.<ShopCategoryMap, QShopCategoryMap>createList("shopCategoryMaps", ShopCategoryMap.class, QShopCategoryMap.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QShopCategory(String variable) {
        super(ShopCategory.class, forVariable(variable));
    }

    public QShopCategory(Path<? extends ShopCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QShopCategory(PathMetadata metadata) {
        super(ShopCategory.class, metadata);
    }

}

