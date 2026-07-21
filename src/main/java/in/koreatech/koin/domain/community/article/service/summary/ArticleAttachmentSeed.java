package in.koreatech.koin.domain.community.article.service.summary;

import java.time.LocalDateTime;

import in.koreatech.koin.domain.community.article.model.ArticleAttachment;

public record ArticleAttachmentSeed(
    Integer id,
    String name,
    String url,
    String hash,
    LocalDateTime updatedAt
) {

    public static ArticleAttachmentSeed from(ArticleAttachment attachment) {
        return new ArticleAttachmentSeed(
            attachment.getId(),
            attachment.getName(),
            attachment.getUrl(),
            attachment.getHash(),
            attachment.getUpdatedAt()
        );
    }
}
