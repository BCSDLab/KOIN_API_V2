package in.koreatech.koin.domain.community.keywords.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordSuggestCache;

public interface ArticleKeywordSuggestRepository extends Repository<ArticleKeywordSuggestCache, String> {

    List<ArticleKeywordSuggestCache> findTop15ByOrderByCountDesc();

    void deleteAll();

    void save(ArticleKeywordSuggestCache hotKeyword);

    List<ArticleKeywordSuggestCache> findAll();

    void delete(ArticleKeywordSuggestCache hotKeyword);
}
