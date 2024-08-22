package in.koreatech.koin.domain.community.keywords.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordSuggest;

public interface ArticleKeywordSuggestRepository extends Repository<ArticleKeywordSuggest, String> {

    List<ArticleKeywordSuggest> findTop15ByOrderByCountDesc();

    void deleteAll();

    void saveAll(List<ArticleKeywordSuggest> hotKeywords);
}
