package in.koreatech.koin.domain.community.article.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArticleSearchKeywordIpMap is a Querydsl query type for ArticleSearchKeywordIpMap
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArticleSearchKeywordIpMap extends EntityPathBase<ArticleSearchKeywordIpMap> {

    private static final long serialVersionUID = -1121231316L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QArticleSearchKeywordIpMap articleSearchKeywordIpMap = new QArticleSearchKeywordIpMap("articleSearchKeywordIpMap");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final QArticleSearchKeyword articleSearchKeyword;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath ipAddress = createString("ipAddress");

    public final NumberPath<Integer> searchCount = createNumber("searchCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QArticleSearchKeywordIpMap(String variable) {
        this(ArticleSearchKeywordIpMap.class, forVariable(variable), INITS);
    }

    public QArticleSearchKeywordIpMap(Path<? extends ArticleSearchKeywordIpMap> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QArticleSearchKeywordIpMap(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QArticleSearchKeywordIpMap(PathMetadata metadata, PathInits inits) {
        this(ArticleSearchKeywordIpMap.class, metadata, inits);
    }

    public QArticleSearchKeywordIpMap(Class<? extends ArticleSearchKeywordIpMap> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.articleSearchKeyword = inits.isInitialized("articleSearchKeyword") ? new QArticleSearchKeyword(forProperty("articleSearchKeyword")) : null;
    }

}

