package in.koreatech.koin.domain.community.article.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.KoinArticle;

public interface KoinArticleRepository extends Repository<KoinArticle, Integer> {

    Optional<KoinArticle> findById(Integer id);

    KoinArticle save(KoinArticle article);

    default KoinArticle getById(Integer articleId) {
        return findById(articleId).orElseThrow(/**/);
    }
}
