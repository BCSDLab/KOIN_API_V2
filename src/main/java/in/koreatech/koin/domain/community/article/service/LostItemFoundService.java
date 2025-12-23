package in.koreatech.koin.domain.community.article.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.dto.FoundLostItemArticleCountResponse;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemFoundService {

    private final ArticleRepository articleRepository;
    public final LostItemArticleRepository lostItemArticleRepository;

    @Transactional
    public void markAsFound(Integer userId, Integer articleId) {
        LostItemArticle lostItemArticle = articleRepository.getById(articleId).getLostItemArticle();
        lostItemArticle.checkOwnership(userId);

        if (lostItemArticle.getIsFound()) {
            throw CustomException.of(ApiResponseCode.DUPLICATE_FOUND_STATUS);
        }

        lostItemArticle.markAsFound();
    }

    @Transactional(readOnly = true)
    public FoundLostItemArticleCountResponse countFoundArticles() {
        Integer foundCount = lostItemArticleRepository.getFoundLostItemArticleCount();
        return new FoundLostItemArticleCountResponse(foundCount);
    }
}
