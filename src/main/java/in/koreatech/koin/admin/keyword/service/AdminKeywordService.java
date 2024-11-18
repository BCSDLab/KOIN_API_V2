package in.koreatech.koin.admin.keyword.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.keyword.repository.AdminArticleKeywordRepository;
import in.koreatech.koin.admin.keyword.dto.AdminFilteredKeywordsResponse;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminKeywordService {

    private final AdminArticleKeywordRepository adminArticleKeywordRepository;

    @Transactional
    public void filterKeyword(String keyword, Boolean isFiltered) {
        ArticleKeyword articleKeyword = adminArticleKeywordRepository.getByKeyword(keyword);

        if (Objects.equals(articleKeyword.getIsFiltered(), isFiltered)) {
            throw new KoinIllegalArgumentException(
                isFiltered ? "이미 필터링 된 키워드입니다: " + keyword : "이미 필터링이 취소된 키워드입니다: " + keyword
            );
        }

        articleKeyword.applyFiltered(isFiltered);
    }

    public AdminFilteredKeywordsResponse getFilteredKeywords() {
        List<ArticleKeyword> filteredKeywords = adminArticleKeywordRepository.findByIsFiltered(true);
        return AdminFilteredKeywordsResponse.from(filteredKeywords);
    }
}
