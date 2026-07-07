package in.koreatech.koin.domain.community.article.service.summary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ArticleSummarySource(
    Integer articleId,
    String title,
    String contentText,
    String author,
    LocalDate registeredAt,
    LocalDateTime updatedAt,
    List<String> attachmentTexts,
    boolean hasTemporaryAttachmentFailure,
    String fingerprint
) {

    public String mergedText() {
        String attachmentText = String.join("\n", attachmentTexts);
        if (attachmentText.isBlank()) {
            return contentText;
        }
        return contentText + "\n" + attachmentText;
    }
}
