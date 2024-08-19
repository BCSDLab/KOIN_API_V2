package in.koreatech.koin.domain.community.keywords.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.keywords.model.ArticleKeyword;

public interface ArticleKeywordRepository extends Repository<ArticleKeyword, Integer> {

    Optional<ArticleKeyword> findByKeyword(String keyword);

    ArticleKeyword save(ArticleKeyword articleKeyword);

    void deleteById(Integer id);

    Optional<ArticleKeyword> findById(Integer id);
}
