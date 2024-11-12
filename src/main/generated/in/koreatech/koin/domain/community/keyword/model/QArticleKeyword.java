package in.koreatech.koin.domain.community.keyword.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QArticleKeyword is a Querydsl query type for ArticleKeyword
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArticleKeyword extends EntityPathBase<ArticleKeyword> {

    private static final long serialVersionUID = 1968596926L;

    public static final QArticleKeyword articleKeyword = new QArticleKeyword("articleKeyword");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    public final ListPath<ArticleKeywordUserMap, QArticleKeywordUserMap> articleKeywordUserMaps = this.<ArticleKeywordUserMap, QArticleKeywordUserMap>createList("articleKeywordUserMaps", ArticleKeywordUserMap.class, QArticleKeywordUserMap.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath keyword = createString("keyword");

    public final DateTimePath<java.time.LocalDateTime> lastUsedAt = createDateTime("lastUsedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QArticleKeyword(String variable) {
        super(ArticleKeyword.class, forVariable(variable));
    }

    public QArticleKeyword(Path<? extends ArticleKeyword> path) {
        super(path.getType(), path.getMetadata());
    }

    public QArticleKeyword(PathMetadata metadata) {
        super(ArticleKeyword.class, metadata);
    }

}

