package in.koreatech.koin.domain.community.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordExtractor {

    private static final int KEYWORD_BATCH_SIZE = 100;

    private final ArticleKeywordRepository articleKeywordRepository;

    public List<ArticleKeywordEvent> matchKeyword(List<Article> articles, Integer authorId) {
        List<ArticleKeywordEvent> keywordEvents = new ArrayList<>();
        int offset = 0;

        while (true) {
            Pageable pageable = PageRequest.of(offset / KEYWORD_BATCH_SIZE, KEYWORD_BATCH_SIZE);
            List<ArticleKeyword> keywords = articleKeywordRepository.findAll(pageable);

            if (keywords.isEmpty()) {
                break;
            }

            for (Article article : articles) {
                String title = article.getTitle();
                for (ArticleKeyword keyword : keywords) {
                    if (title.contains(keyword.getKeyword())) {
                        keywordEvents.add(new ArticleKeywordEvent(article.getId(), authorId, keyword));
                    }
                }
            }
            offset += KEYWORD_BATCH_SIZE;
        }

        return keywordEvents;
    }
}
