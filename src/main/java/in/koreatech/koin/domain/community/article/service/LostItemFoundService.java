package in.koreatech.koin.domain.community.article.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.dto.FoundLostItemArticleCountResponse;
import in.koreatech.koin.domain.community.article.dto.NotFoundLostItemArticleCountResponse;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemFoundService {

    private final ArticleRepository articleRepository;
    private final LostItemArticleRepository lostItemArticleRepository;

    @Transactional
    public void markAsFound(Integer userId, Integer articleId) {
        LostItemArticle lostItemArticle = articleRepository.getById(articleId).getLostItemArticle();
        lostItemArticle.checkOwnership(userId);
        lostItemArticle.markAsFound();
    }

    @Transactional(readOnly = true)
    public FoundLostItemArticleCountResponse countFoundArticles() {
        Integer foundCount = lostItemArticleRepository.getFoundLostItemArticleCount();
        return new FoundLostItemArticleCountResponse(foundCount);
    }

    @Transactional(readOnly = true)
    public NotFoundLostItemArticleCountResponse countNotFoundArticles() {
        Integer foundCount = lostItemArticleRepository.getNotFoundLostItemArticleCount();
        return new NotFoundLostItemArticleCountResponse(foundCount);
    }
}
