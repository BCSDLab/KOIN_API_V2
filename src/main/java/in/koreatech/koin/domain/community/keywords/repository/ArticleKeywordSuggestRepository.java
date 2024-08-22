package in.koreatech.koin.domain.community.keywords.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordSuggest;

public interface ArticleKeywordSuggestRepository extends Repository<ArticleKeywordSuggest, String> {

    List<ArticleKeywordSuggest> findTop15ByOrderByCountDesc();

    void deleteAll();

    void save(ArticleKeywordSuggest hotKeyword);

    List<ArticleKeywordSuggest> findAll();

    void delete(ArticleKeywordSuggest hotKeyword);
}
