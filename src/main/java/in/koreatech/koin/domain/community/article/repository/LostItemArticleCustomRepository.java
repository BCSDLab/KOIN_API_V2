package in.koreatech.koin.domain.community.article.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import in.koreatech.koin.domain.community.article.dto.LostItemArticleSummary;
import in.koreatech.koin.domain.community.article.model.Article;

public interface LostItemArticleCustomRepository {

    LostItemArticleSummary getArticleSummary(Integer articleId);

    Long countLostItemArticlesWithFilters(String type, Boolean isFound, Integer lostItemArticleBoardId);

    Page<Article> findLostItemArticlesWithFilters(Integer boardId, String type, Boolean isFound, PageRequest pageRequest);
}
