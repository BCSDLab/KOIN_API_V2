package in.koreatech.koin.domain.community.lostitem.chatroom.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemArticleInfoService {

    private final LostItemArticleRepository lostItemArticleRepository;
    private final ArticleRepository articleRepository;


    public String getArticleTitle(Integer articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow(
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_ARTICLE)
        );

        return article.getTitle();
    }

    public String getChatPartnerProfileImage(Integer articleId) {
        LostItemArticle lostItemArticle = lostItemArticleRepository.findByArticleId(articleId).orElseThrow(
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_ARTICLE)
        );

        return lostItemArticle.getAuthor().getProfileImageUrl();
    }
}
