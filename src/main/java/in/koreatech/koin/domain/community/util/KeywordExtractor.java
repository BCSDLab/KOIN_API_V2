package in.koreatech.koin.domain.community.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.keyword.enums.KeywordCategory;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordExtractor {

    private static final int KEYWORD_BATCH_SIZE = 100;

    private final ArticleKeywordRepository articleKeywordRepository;

    public List<String> matchKeywords(String title, KeywordCategory category) {
        List<String> matchedKeywords = new ArrayList<>();
        int offset = 0;

        while (true) {
            Pageable pageable = PageRequest.of(offset / KEYWORD_BATCH_SIZE, KEYWORD_BATCH_SIZE);
            List<ArticleKeyword> keywords = articleKeywordRepository.findAllByCategory(category, pageable);

            if (keywords.isEmpty()) {
                break;
            }
            for (ArticleKeyword keyword : keywords) {
                if (title.contains(keyword.getKeyword())) {
                    matchedKeywords.add(keyword.getKeyword());
                }
            }
            offset += KEYWORD_BATCH_SIZE;
        }

        return matchedKeywords;
    }
}
