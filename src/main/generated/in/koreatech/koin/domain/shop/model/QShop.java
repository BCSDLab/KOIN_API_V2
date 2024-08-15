package in.koreatech.koin.domain.shop.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QShop is a Querydsl query type for Shop
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QShop extends EntityPathBase<Shop> {

    private static final long serialVersionUID = 821478615L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QShop shop = new QShop("shop");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final StringPath accountNumber = createString("accountNumber");

    public final StringPath address = createString("address");

    public final StringPath bank = createString("bank");

    public final StringPath chosung = createString("chosung");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath delivery = createBoolean("delivery");

    public final NumberPath<Integer> deliveryPrice = createNumber("deliveryPrice", Integer.class);

    public final StringPath description = createString("description");

    public final ListPath<EventArticle, QEventArticle> eventArticles = this.<EventArticle, QEventArticle>createList("eventArticles", EventArticle.class, QEventArticle.class, PathInits.DIRECT2);

    public final NumberPath<Integer> hit = createNumber("hit", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath internalName = createString("internalName");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final BooleanPath isEvent = createBoolean("isEvent");

    public final ListPath<MenuCategory, QMenuCategory> menuCategories = this.<MenuCategory, QMenuCategory>createList("menuCategories", MenuCategory.class, QMenuCategory.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final in.koreatech.koin.domain.owner.model.QOwner owner;

    public final BooleanPath payBank = createBoolean("payBank");

    public final BooleanPath payCard = createBoolean("payCard");

    public final StringPath phone = createString("phone");

    public final StringPath remarks = createString("remarks");

    public final ListPath<ShopReview, QShopReview> reviews = this.<ShopReview, QShopReview>createList("reviews", ShopReview.class, QShopReview.class, PathInits.DIRECT2);

    public final SetPath<ShopCategoryMap, QShopCategoryMap> shopCategories = this.<ShopCategoryMap, QShopCategoryMap>createSet("shopCategories", ShopCategoryMap.class, QShopCategoryMap.class, PathInits.DIRECT2);

    public final ListPath<ShopImage, QShopImage> shopImages = this.<ShopImage, QShopImage>createList("shopImages", ShopImage.class, QShopImage.class, PathInits.DIRECT2);

    public final ListPath<ShopOpen, QShopOpen> shopOpens = this.<ShopOpen, QShopOpen>createList("shopOpens", ShopOpen.class, QShopOpen.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QShop(String variable) {
        this(Shop.class, forVariable(variable), INITS);
    }

    public QShop(Path<? extends Shop> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QShop(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QShop(PathMetadata metadata, PathInits inits) {
        this(Shop.class, metadata, inits);
    }

    public QShop(Class<? extends Shop> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new in.koreatech.koin.domain.owner.model.QOwner(forProperty("owner"), inits.get("owner")) : null;
    }

}

