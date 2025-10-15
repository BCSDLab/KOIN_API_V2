package in.koreatech.koin.domain.community.keyword.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordSuggestCache;

public interface ArticleKeywordSuggestRedisRepository extends Repository<ArticleKeywordSuggestCache, String> {

    List<ArticleKeywordSuggestCache> findTop15ByOrderByCountDesc();

    void deleteAll();

    void save(ArticleKeywordSuggestCache hotKeyword);

    List<ArticleKeywordSuggestCache> findAll();

    void delete(ArticleKeywordSuggestCache hotKeyword);
}
