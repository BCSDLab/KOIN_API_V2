package in.koreatech.koin.domain.community.article.service.summary;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import in.koreatech.koin.infrastructure.s3.client.S3Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleSummarySourceReader {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "bmp", "pdf", "tiff", "tif", "heic", "docx", "pptx", "xlsx", "hwp", "hwpx"
    );

    private final ArticleDocumentParseClient documentParseClient;
    private final ArticleAiSummaryProperties properties;
    private final S3Client s3Client;

    public String createFingerprint(ArticleSummarySourceSeed seed) {
        StringBuilder builder = new StringBuilder();
        builder.append("articleId=").append(seed.articleId()).append('\n');
        builder.append("title=").append(nullToEmpty(seed.title())).append('\n');
        builder.append("content=").append(nullToEmpty(seed.content())).append('\n');
        seed.attachments().stream()
            .sorted(Comparator.comparing(ArticleAttachmentSeed::id))
            .forEach(attachment -> builder
                .append("attachment=")
                .append(attachment.id()).append('|')
                .append(nullToEmpty(attachment.name())).append('|')
                .append(nullToEmpty(attachment.url())).append('|')
                .append(nullToEmpty(attachment.hash())).append('|')
                .append(attachment.updatedAt())
                .append('\n'));

        extractImageUrls(seed.content()).forEach(url -> builder.append("image=").append(url).append('\n'));
        return sha256(builder.toString());
    }

    public ArticleSummarySource read(ArticleSummarySourceSeed seed) {
        String resolvedContent = resolveContent(seed.content());
        ArticleSummarySourceSeed resolvedSeed = new ArticleSummarySourceSeed(
            seed.articleId(),
            seed.title(),
            resolvedContent,
            seed.author(),
            seed.registeredAt(),
            seed.updatedAt(),
            seed.attachments()
        );
        String contentText = htmlToText(resolvedContent);
        String fingerprint = createFingerprint(resolvedSeed);
        List<String> attachmentTexts = readAttachmentTexts(resolvedSeed);
        return new ArticleSummarySource(
            seed.articleId(),
            seed.title(),
            contentText,
            seed.author(),
            seed.registeredAt(),
            seed.updatedAt(),
            attachmentTexts,
            fingerprint
        );
    }

    private String resolveContent(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String trimmed = content.trim();
        if (!trimmed.startsWith("https")) {
            return content;
        }
        if (!trimmed.startsWith(s3Client.getDomainUrlPrefix())) {
            log.warn("허용되지 않은 게시글 본문 URL을 요약 입력에서 제외했습니다. url: {}", sanitizeUrl(trimmed));
            return "";
        }
        try {
            return s3Client.getContentFromUrl(trimmed);
        } catch (Exception e) {
            log.warn("게시글 본문 URL 조회에 실패했습니다. url: {}", sanitizeUrl(trimmed), e);
            return "";
        }
    }

    private List<String> readAttachmentTexts(ArticleSummarySourceSeed seed) {
        List<DocumentParseRequest> parseRequests = new ArrayList<>();
        seed.attachments().forEach(attachment -> {
            if (isSupported(attachment.url(), attachment.name())) {
                parseRequests.add(new DocumentParseRequest(attachment.url(), attachment.name()));
            }
        });
        extractImageUrls(seed.content()).forEach(url -> {
            if (isSupported(url, url)) {
                parseRequests.add(new DocumentParseRequest(url, fileNameFromUrl(url)));
            }
        });

        return parseRequests.stream()
            .limit(properties.getMaxDocumentsPerArticle())
            .map(this::parseDocument)
            .filter(StringUtils::hasText)
            .toList();
    }

    private String parseDocument(DocumentParseRequest request) {
        try {
            return documentParseClient.parse(request);
        } catch (Exception e) {
            log.warn("게시글 첨부 문서 파싱에 실패했습니다. articleDocument: {}", sanitizeUrl(request.url()), e);
            return "";
        }
    }

    private List<String> extractImageUrls(String html) {
        if (!StringUtils.hasText(html)) {
            return List.of();
        }
        Document document = Jsoup.parse(html);
        return document.select("img[src]")
            .eachAttr("src")
            .stream()
            .map(String::trim)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();
    }

    private String htmlToText(String html) {
        if (!StringUtils.hasText(html)) {
            return "";
        }
        return Jsoup.parse(html).text().trim();
    }

    private boolean isSupported(String url, String fileName) {
        if (!isAllowedDocumentUrl(url)) {
            return false;
        }
        String extension = extensionOf(StringUtils.hasText(fileName) ? fileName : url);
        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    private boolean isAllowedDocumentUrl(String url) {
        if (!StringUtils.hasText(url) || !url.startsWith("https://")) {
            return false;
        }
        if (url.startsWith(s3Client.getDomainUrlPrefix())) {
            return true;
        }
        return properties.getAllowedDocumentUrlPrefixes().stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .anyMatch(url::startsWith);
    }

    private String extensionOf(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == normalized.length() - 1) {
            return "";
        }
        return normalized.substring(dotIndex + 1);
    }

    private String fileNameFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "document";
        }
        int slashIndex = url.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex == url.length() - 1) {
            return "document";
        }
        return url.substring(slashIndex + 1);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new KoinIllegalStateException("요약 원문 해시 생성에 실패했습니다. " + e.getMessage());
        }
    }

    private String nullToEmpty(Object value) {
        return Objects.toString(value, "");
    }

    private String sanitizeUrl(String url) {
        int queryIndex = url.indexOf('?');
        if (queryIndex < 0) {
            return url;
        }
        return url.substring(0, queryIndex) + "?<redacted>";
    }
}
