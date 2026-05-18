package in.koreatech.koin.domain.community.article.service.summary;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class ArticleSummaryContentRenderer {

    public String prependSummary(String content, List<String> summaryLines) {
        if (summaryLines == null || summaryLines.isEmpty()) {
            return content;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<div class=\"ai-summary\">\n");
        builder.append("  <p><strong>✨ AI 요약</strong></p>\n");
        for (int i = 0; i < summaryLines.size(); i++) {
            builder.append("  <p>")
                .append(i + 1)
                .append(". ")
                .append(HtmlUtils.htmlEscape(summaryLines.get(i)))
                .append("</p>\n");
        }
        builder.append("</div>\n<hr>\n");
        builder.append(content == null ? "" : content);
        return builder.toString();
    }
}
