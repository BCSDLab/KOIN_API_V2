package in.koreatech.koin.domain.community.article.service.summary;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

@Component
public class ArticleSummaryContentRenderer {

    private static final int MAX_SUMMARY_LINES = 3;
    private static final int MAX_SUMMARY_LINE_LENGTH = 220;
    private static final List<String> ALLOWED_LINE_PREFIXES = List.of(
        "📅 ", "🎯 ", "📍 ", "📝 ", "💰 ", "📌 ", "📎 ", "✅ "
    );
    private static final String SUMMARY_CONTAINER_STYLE =
        "max-width:100%;margin:24px 0 28px;padding:0;background-color:transparent;"
            + "color:#292e36;font-family:pretendard,-apple-system,BlinkMacSystemFont,"
            + "'Apple SD Gothic Neo','Noto Sans KR',sans-serif;letter-spacing:0;"
            + "text-align:left;text-indent:0;white-space:normal;box-sizing:border-box;";
    private static final String SUMMARY_TITLE_STYLE =
        "margin:0 0 15px;color:#17191d;font-size:16px;font-weight:700;line-height:1.5;";
    private static final String SUMMARY_TITLE_LABEL_STYLE =
        "display:inline-block;padding:0 0 6px;border-bottom:2px solid #0054a6;";
    private static final String SUMMARY_LINE_STYLE =
        "display:flex;align-items:flex-start;gap:8px;color:#343942;font-size:15px;"
            + "font-weight:400;line-height:1.7;word-break:keep-all;word-wrap:break-word;"
            + "overflow-wrap:break-word;";
    private static final String SUMMARY_ICON_STYLE =
        "display:block;flex:0 0 20px;width:20px;text-align:left;";
    private static final String SUMMARY_TEXT_STYLE =
        "display:block;min-width:0;flex:1 1 auto;";

    public String prependSummary(String content, List<String> summaryLines) {
        List<String> renderableSummaryLines = sanitize(summaryLines);
        if (renderableSummaryLines.isEmpty()) {
            return content;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"ai-summary\" role=\"note\" aria-label=\"AI 요약\" lang=\"ko\" style=\"")
            .append(SUMMARY_CONTAINER_STYLE)
            .append("\">\n");
        builder.append("  <p style=\"")
            .append(SUMMARY_TITLE_STYLE)
            .append("\">\n")
            .append("    <span style=\"")
            .append(SUMMARY_TITLE_LABEL_STYLE)
            .append("\">AI 요약</span>\n")
            .append("  </p>\n");
        for (int index = 0; index < renderableSummaryLines.size(); index++) {
            appendSummaryLine(
                builder,
                renderableSummaryLines.get(index),
                index == renderableSummaryLines.size() - 1
            );
        }
        builder.append("</div>\n");
        builder.append(content == null ? "" : content);
        return builder.toString();
    }

    private void appendSummaryLine(StringBuilder builder, String summaryLine, boolean isLastLine) {
        String prefix = findAllowedPrefix(summaryLine).orElseThrow();
        String icon = prefix.trim();
        String text = summaryLine.substring(prefix.length());
        String margin = isLastLine ? "margin:0;" : "margin:0 0 11px;";

        builder.append("  <p style=\"")
            .append(SUMMARY_LINE_STYLE)
            .append(margin)
            .append("\">\n")
            .append("    <span aria-hidden=\"true\" style=\"")
            .append(SUMMARY_ICON_STYLE)
            .append("\">")
            .append(HtmlUtils.htmlEscape(icon))
            .append("</span>\n")
            .append("    <span style=\"")
            .append(SUMMARY_TEXT_STYLE)
            .append("\">")
            .append(HtmlUtils.htmlEscape(text))
            .append("</span>\n")
            .append("  </p>\n");
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
        return findAllowedPrefix(summaryLine).isPresent();
    }

    private boolean hasTextAfterPrefix(String summaryLine) {
        return findAllowedPrefix(summaryLine)
            .map(prefix -> summaryLine.substring(prefix.length()))
            .filter(StringUtils::hasText)
            .isPresent();
    }

    private Optional<String> findAllowedPrefix(String summaryLine) {
        return ALLOWED_LINE_PREFIXES.stream()
            .filter(summaryLine::startsWith)
            .findFirst();
    }
}
