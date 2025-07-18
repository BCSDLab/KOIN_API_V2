package in.koreatech.koin.domain.community.article.repository;

import static in.koreatech.koin.domain.community.article.model.QArticle.article;
import static in.koreatech.koin.domain.community.article.model.QLostItemArticle.lostItemArticle;
import static in.koreatech.koin.domain.community.article.model.QLostItemImage.lostItemImage;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.community.article.dto.LostItemArticleSummary;
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
}
