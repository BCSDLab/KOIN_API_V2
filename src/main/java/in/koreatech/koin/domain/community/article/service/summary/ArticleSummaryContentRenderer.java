package in.koreatech.koin.domain.community.article.service.summary;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

@Component
public class ArticleSummaryContentRenderer {

    private static final int MAX_SUMMARY_LINES = 3;
    private static final int MAX_SUMMARY_LINE_LENGTH = 220;
    private static final Set<String> ALLOWED_LINE_PREFIXES = Set.of(
        "📅 ", "🎯 ", "📍 ", "📝 ", "💰 ", "📌 ", "📎 ", "✅ "
    );

    public String prependSummary(String content, List<String> summaryLines) {
        List<String> renderableSummaryLines = sanitize(summaryLines);
        if (renderableSummaryLines.isEmpty()) {
            return content;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"ai-summary\">\n");
        builder.append("  <p><strong>✨ AI 요약</strong></p>\n");
        for (String summaryLine : renderableSummaryLines) {
            builder.append("  <p>")
                .append(HtmlUtils.htmlEscape(summaryLine))
                .append("</p>\n");
        }
        builder.append("</div>\n<hr>\n");
        builder.append(content == null ? "" : content);
        return builder.toString();
    }

    private List<String> sanitize(List<String> summaryLines) {
        if (summaryLines == null || summaryLines.isEmpty()) {
            return List.of();
        }
        return summaryLines.stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .filter(this::hasAllowedPrefix)
            .filter(this::hasTextAfterPrefix)
            .filter(summaryLine -> summaryLine.length() <= MAX_SUMMARY_LINE_LENGTH)
            .limit(MAX_SUMMARY_LINES)
            .toList();
    }

    private boolean hasAllowedPrefix(String summaryLine) {
        return ALLOWED_LINE_PREFIXES.stream().anyMatch(summaryLine::startsWith);
    }

    private boolean hasTextAfterPrefix(String summaryLine) {
        return ALLOWED_LINE_PREFIXES.stream()
            .filter(summaryLine::startsWith)
            .findFirst()
            .map(prefix -> summaryLine.substring(prefix.length()))
            .filter(StringUtils::hasText)
            .isPresent();
    }
}
