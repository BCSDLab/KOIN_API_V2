package in.koreatech.koin.domain.community.keyword.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArticleKeywordUserMap is a Querydsl query type for ArticleKeywordUserMap
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArticleKeywordUserMap extends EntityPathBase<ArticleKeywordUserMap> {

    private static final long serialVersionUID = 176834547L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QArticleKeywordUserMap articleKeywordUserMap = new QArticleKeywordUserMap("articleKeywordUserMap");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final QArticleKeyword articleKeyword;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final in.koreatech.koin.domain.user.model.QUser user;

    public QArticleKeywordUserMap(String variable) {
        this(ArticleKeywordUserMap.class, forVariable(variable), INITS);
    }

    public QArticleKeywordUserMap(Path<? extends ArticleKeywordUserMap> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QArticleKeywordUserMap(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QArticleKeywordUserMap(PathMetadata metadata, PathInits inits) {
        this(ArticleKeywordUserMap.class, metadata, inits);
    }

    public QArticleKeywordUserMap(Class<? extends ArticleKeywordUserMap> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.articleKeyword = inits.isInitialized("articleKeyword") ? new QArticleKeyword(forProperty("articleKeyword")) : null;
        this.user = inits.isInitialized("user") ? new in.koreatech.koin.domain.user.model.QUser(forProperty("user")) : null;
    }

}

