package in.koreatech.koin.domain.translation.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;

/** HTML markup and attributes stay server-side; only visible text nodes go to Solar. */
public final class ArticleHtmlTranslation {
    private ArticleHtmlTranslation() {}

    public static Map<String, String> texts(String html) {
        Map<String, String> texts = new LinkedHashMap<>();
        var nodes = textNodes(parse(html));
        for (int i = 0; i < nodes.size(); i++) {
            texts.put("text_" + i, nodes.get(i).getWholeText());
        }
        return texts;
    }

    public static String restore(String html, Map<String, String> translated) {
        Document document = parse(html);
        var nodes = textNodes(document);
        for (int i = 0; i < nodes.size(); i++) {
            // TextNode escapes model-produced markup instead of inserting it as HTML.
            nodes.get(i).text(translated.get("text_" + i));
        }
        return isDocument(html) ? document.outerHtml() : document.body().html();
    }

    private static Document parse(String html) {
        String input = html == null ? "" : html;
        Document document = isDocument(input) ? Jsoup.parse(input) : Jsoup.parseBodyFragment(input);
        document.outputSettings().prettyPrint(false);
        return document;
    }

    private static boolean isDocument(String html) {
        return html != null && html.toLowerCase(java.util.Locale.ROOT).contains("<html");
    }

    private static List<TextNode> textNodes(Document document) {
        List<TextNode> nodes = new ArrayList<>();
        for (var element : document.body().getAllElements()) {
            if (element.closest("script, style, noscript, template") != null) {
                continue;
            }
            for (TextNode node : element.textNodes()) {
                if (node.getWholeText().codePoints().anyMatch(Character::isLetter)) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }
}
