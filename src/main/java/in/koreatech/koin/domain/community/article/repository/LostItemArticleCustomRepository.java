package in.koreatech.koin.domain.community.article.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;

import in.koreatech.koin.domain.community.article.dto.LostItemArticleSummary;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.filter.LostItemSortType;

public interface LostItemArticleCustomRepository {

    LostItemArticleSummary getArticleSummary(Integer articleId);

    Long countLostItemArticlesWithFilters(String type, Boolean isFound, String itemCategory,
        Integer lostItemArticleBoardId, Integer authorId, String titleQuery);

    List<Article> findLostItemArticlesWithFilters(Integer boardId, String type, Boolean isFound,
        String itemCategoryFilter, LostItemSortType sort, PageRequest pageRequest, Integer authorId, String titleQuery);
}
