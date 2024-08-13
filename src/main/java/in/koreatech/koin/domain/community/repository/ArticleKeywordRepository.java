package in.koreatech.koin.domain.community.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.model.ArticleKeyword;

public interface ArticleKeywordRepository extends Repository<ArticleKeyword, Integer> {
    ArticleKeyword findByKeyword(String keyword);

    void save(ArticleKeyword build);
}
