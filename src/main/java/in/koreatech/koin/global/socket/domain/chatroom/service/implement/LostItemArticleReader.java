package in.koreatech.koin.global.socket.domain.chatroom.service.implement;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LostItemArticleReader {

    private final LostItemArticleRepository lostItemArticleRepository;
    private final ArticleRepository articleRepository;

    public LostItemArticle readByArticleId(Integer articleId) {
        return lostItemArticleRepository.getByArticleId(articleId);
    }

    public String getArticleTitle(Integer articleId) {
        return articleRepository.getTitleById(articleId);
    }

    public ChatRoomSummary getChatRoomSummary(Integer articleId) {
        LostItemArticle lostItemArticle = readByArticleId(articleId);
        String title = lostItemArticle.getArticle().getTitle();
        String imageUrl = (lostItemArticle.getImages() != null && !lostItemArticle.getImages().isEmpty())
            ? lostItemArticle.getImages().get(0).getImageUrl()
            : null;

        return ChatRoomSummary.builder()
            .articleTitle(title)
            .itemImage(imageUrl)
            .build();
    }

    @Getter
    @Builder
    public static class ChatRoomSummary {
        private String articleTitle;
        private String itemImage;
    }
}
