package in.koreatech.koin.domain.community.article.repository;

import in.koreatech.koin.domain.community.article.dto.LostItemArticleSummary;

public interface LostItemArticleCustomRepository {

    LostItemArticleSummary getArticleSummary(Integer articleId);
}
