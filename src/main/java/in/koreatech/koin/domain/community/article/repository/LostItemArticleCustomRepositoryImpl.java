package in.koreatech.koin.domain.community.article.repository;

import static in.koreatech.koin.domain.community.article.model.QArticle.article;
import static in.koreatech.koin.domain.community.article.model.QLostItemArticle.lostItemArticle;
import static in.koreatech.koin.domain.community.article.model.QLostItemImage.lostItemImage;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.community.article.dto.LostItemArticleSummary;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.filter.LostItemSortType;
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

    public Long countLostItemArticlesWithFilters(String type, Boolean isFound, String itemCategory,
        Integer lostItemArticleBoardId, Integer authorId) {
        BooleanExpression filter = getFilter(lostItemArticleBoardId, type, itemCategory, isFound, authorId);

        return queryFactory
            .select(article.count())
            .from(article)
            .leftJoin(article.lostItemArticle, lostItemArticle)
            .where(filter)
            .fetchOne();
    }

    public List<Article> findLostItemArticlesWithFilters(Integer boardId, String type, Boolean isFound,
        String itemCategory, LostItemSortType sort, PageRequest pageRequest, Integer authorId) {

        BooleanExpression predicate = getFilter(boardId, type, itemCategory, isFound, authorId);
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(sort);

        return queryFactory
            .selectFrom(article)
            .leftJoin(article.lostItemArticle, lostItemArticle).fetchJoin()
            .leftJoin(lostItemArticle.author).fetchJoin()
            .where(predicate)
            .orderBy(orderSpecifiers)
            .offset(pageRequest.getOffset())
            .limit(pageRequest.getPageSize())
            .fetch();
    }

    private BooleanExpression getFilter(Integer boardId, String type, String itemCategory, Boolean isFound, Integer authorId) {
        BooleanExpression filter = article.board.id.eq(boardId)
            .and(article.isDeleted.isFalse())
            .and(article.lostItemArticle.isNotNull());

        if (type != null && !type.isBlank()) {
            filter = filter.and(lostItemArticle.type.eq(type));
        }

        if (isFound != null) {
            filter = filter.and(lostItemArticle.isFound.eq(isFound));
        }

        if (itemCategory != null && !itemCategory.isBlank()) {
            filter = filter.and(lostItemArticle.category.eq(itemCategory));
        }

        if (authorId != null) {
            filter = filter.and(lostItemArticle.author.id.eq(authorId));
        }

        return filter;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(LostItemSortType sort) {
        if (sort == LostItemSortType.OLDEST) {
            return new OrderSpecifier[] {
                article.createdAt.asc(),
                article.id.asc()
            };
        }

        return new OrderSpecifier[] {
            article.createdAt.desc(),
            article.id.desc()
        };
    }
}
