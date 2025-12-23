package in.koreatech.koin.domain.community.article.repository;

import static in.koreatech.koin.domain.community.article.model.QArticle.article;
import static in.koreatech.koin.domain.community.article.model.QLostItemArticle.lostItemArticle;
import static in.koreatech.koin.domain.community.article.model.QLostItemImage.lostItemImage;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.community.article.dto.LostItemArticleSummary;
import in.koreatech.koin.domain.community.article.model.Article;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LostItemArticleCustomRepositoryImpl implements LostItemArticleCustomRepository {

    private final JPAQueryFactory queryFactory;

    public LostItemArticleSummary getArticleSummary(Integer articleId) {
        return queryFactory
            .select(Projections.constructor(LostItemArticleSummary.class,
                article.title,
                lostItemImage.imageUrl.min()
            ))
            .from(lostItemArticle)
            .innerJoin(lostItemArticle.article, article)
            .leftJoin(lostItemArticle.images, lostItemImage)
            .where(article.id.eq(articleId))
            .fetchFirst();
    }

    public Long countLostItemArticlesWithFilters(String type, Boolean isFound, Integer lostItemArticleBoardId) {
        BooleanExpression filter = getFilter(lostItemArticleBoardId, type, isFound);

        return queryFactory
            .select(article.count())
            .from(article)
            .leftJoin(article.lostItemArticle, lostItemArticle)
            .where(filter)
            .fetchOne();
    }

    public List<Article> findLostItemArticlesWithFilters(
        Integer boardId, String type, Boolean isFound, PageRequest pageRequest) {

        BooleanExpression predicate = getFilter(boardId, type, isFound);

        return queryFactory
            .selectFrom(article)
            .leftJoin(article.lostItemArticle, lostItemArticle).fetchJoin()
            .leftJoin(lostItemArticle.author).fetchJoin()
            .where(predicate)
            .orderBy(article.createdAt.desc(), article.id.desc())
            .offset(pageRequest.getOffset())
            .limit(pageRequest.getPageSize())
            .fetch();
    }

    private BooleanExpression getFilter(Integer boardId, String type, Boolean isFound) {
        BooleanExpression filter = article.board.id.eq(boardId)
            .and(article.isDeleted.isFalse())
            .and(article.lostItemArticle.isNotNull());

        if (type != null && !type.isBlank()) {
            filter = filter.and(lostItemArticle.type.eq(type));
        }

        if (isFound != null) {
            filter = filter.and(lostItemArticle.isFound.eq(isFound));
        }

        return filter;
    }
}
