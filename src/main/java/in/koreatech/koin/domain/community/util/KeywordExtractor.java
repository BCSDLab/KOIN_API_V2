package in.koreatech.koin.domain.community.util;

import java.util.ArrayList;
import java.util.List;

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

    private final ArticleKeywordRepository articleKeywordRepository;

    public List<String> matchKeywords(String title, KeywordCategory category) {
        List<String> matchedKeywords = new ArrayList<>();
        List<ArticleKeyword> keywords = articleKeywordRepository.findAllByCategory(category);

        for (ArticleKeyword keyword : keywords) {
            if (title.contains(keyword.getKeyword())) {
                matchedKeywords.add(keyword.getKeyword());
            }
        }

        return matchedKeywords;
    }
}
