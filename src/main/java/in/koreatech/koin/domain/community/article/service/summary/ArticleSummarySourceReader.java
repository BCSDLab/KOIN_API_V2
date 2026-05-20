package in.koreatech.koin.domain.community.article.service.summary;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
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

        extractInlineDocumentUrls(seed.content()).forEach(url -> builder.append("inlineDocument=").append(url).append('\n'));
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
        AttachmentReadResult attachmentReadResult = readAttachmentTexts(resolvedSeed);
        return new ArticleSummarySource(
            seed.articleId(),
            seed.title(),
            contentText,
            seed.author(),
            seed.registeredAt(),
            seed.updatedAt(),
            attachmentReadResult.texts(),
            attachmentReadResult.hasTemporaryFailure(),
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
        if (!isAllowedContentUrl(trimmed)) {
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

    private boolean isAllowedContentUrl(String url) {
        if (!StringUtils.hasText(url) || !url.startsWith("https://")) {
            return false;
        }
        if (url.startsWith(s3Client.getDomainUrlPrefix())) {
            return true;
        }
        return properties.getAllowedContentUrlPrefixes().stream()
            .filter(StringUtils::hasText)
            .map(String::trim)
            .anyMatch(url::startsWith);
    }

    private AttachmentReadResult readAttachmentTexts(ArticleSummarySourceSeed seed) {
        List<DocumentParseRequest> parseRequests = new ArrayList<>();
        Set<String> parseUrls = new LinkedHashSet<>();
        seed.attachments().forEach(attachment -> {
            if (isSupported(attachment.url(), attachment.name()) && parseUrls.add(attachment.url())) {
                parseRequests.add(new DocumentParseRequest(attachment.url(), attachment.name()));
            }
        });
        extractInlineDocumentUrls(seed.content()).forEach(url -> {
            if (isSupported(url, url) && parseUrls.add(url)) {
                parseRequests.add(new DocumentParseRequest(url, fileNameFromUrl(url)));
            }
        });

        List<String> texts = new ArrayList<>();
        boolean hasTemporaryFailure = false;
        for (DocumentParseRequest request : parseRequests.stream()
            .limit(properties.getMaxDocumentsPerArticle())
            .toList()) {
            ParsedDocument parsedDocument = parseDocument(request);
            if (StringUtils.hasText(parsedDocument.text())) {
                texts.add(parsedDocument.text());
            }
            if (parsedDocument.temporaryFailure()) {
                hasTemporaryFailure = true;
            }
        }
        return new AttachmentReadResult(texts, hasTemporaryFailure);
    }

    private ParsedDocument parseDocument(DocumentParseRequest request) {
        try {
            String parsedText = documentParseClient.parse(request);
            if (!StringUtils.hasText(parsedText)) {
                return ParsedDocument.empty();
            }
            return new ParsedDocument(
                "파일명: " + fileNameForPrompt(request) + "\n추출 내용:\n" + parsedText,
                false
            );
        } catch (ArticleSummaryExternalApiException e) {
            log.warn("게시글 첨부 문서 파싱에 실패했습니다. articleDocument: {}", sanitizeUrl(request.url()), e);
            return new ParsedDocument("", e.isRetryable());
        } catch (Exception e) {
            log.warn("게시글 첨부 문서 파싱에 실패했습니다. articleDocument: {}", sanitizeUrl(request.url()), e);
            return ParsedDocument.empty();
        }
    }

    private List<String> extractInlineDocumentUrls(String html) {
        if (!StringUtils.hasText(html)) {
            return List.of();
        }
        Document document = Jsoup.parse(html);
        Set<String> urls = new LinkedHashSet<>();
        collectUrls(document, "img[src]", element -> element.attr("src"), urls);
        collectUrls(document, "a[href]", element -> element.attr("href"), urls);
        collectUrls(document, "iframe[src]", element -> element.attr("src"), urls);
        collectUrls(document, "embed[src]", element -> element.attr("src"), urls);
        collectUrls(document, "object[data]", element -> element.attr("data"), urls);
        return urls.stream()
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }

    private void collectUrls(
        Document document,
        String cssQuery,
        Function<org.jsoup.nodes.Element, String> urlExtractor,
        Set<String> urls
    ) {
        document.select(cssQuery).forEach(element -> Optional.ofNullable(urlExtractor.apply(element))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .ifPresent(urls::add));
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
        String normalized = url;
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex == normalized.length() - 1) {
            return "document";
        }
        return normalized.substring(slashIndex + 1);
    }

    private String fileNameForPrompt(DocumentParseRequest request) {
        if (StringUtils.hasText(request.fileName())) {
            return request.fileName();
        }
        return fileNameFromUrl(request.url());
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

    private record AttachmentReadResult(
        List<String> texts,
        boolean hasTemporaryFailure
    ) {
    }

    private record ParsedDocument(
        String text,
        boolean temporaryFailure
    ) {

        private static ParsedDocument empty() {
            return new ParsedDocument("", false);
        }
    }
}
