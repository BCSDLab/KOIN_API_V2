package in.koreatech.koin.domain.community.article.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QArticleSearchKeyword is a Querydsl query type for ArticleSearchKeyword
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QArticleSearchKeyword extends EntityPathBase<ArticleSearchKeyword> {

    private static final long serialVersionUID = -446215671L;

    public static final QArticleSearchKeyword articleSearchKeyword = new QArticleSearchKeyword("articleSearchKeyword");

    public final in.koreatech.koin.global.domain.QBaseEntity _super = new in.koreatech.koin.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath keyword = createString("keyword");

    public final DateTimePath<java.time.LocalDateTime> lastSearchedAt = createDateTime("lastSearchedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> totalSearch = createNumber("totalSearch", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Double> weight = createNumber("weight", Double.class);

    public QArticleSearchKeyword(String variable) {
        super(ArticleSearchKeyword.class, forVariable(variable));
    }

    public QArticleSearchKeyword(Path<? extends ArticleSearchKeyword> path) {
        super(path.getType(), path.getMetadata());
    }

    public QArticleSearchKeyword(PathMetadata metadata) {
        super(ArticleSearchKeyword.class, metadata);
    }

}

