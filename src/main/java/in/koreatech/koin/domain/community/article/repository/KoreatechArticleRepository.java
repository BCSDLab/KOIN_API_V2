package in.koreatech.koin.domain.community.article.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.KoreatechArticle;

public interface KoreatechArticleRepository extends Repository<KoreatechArticle, Integer> {

    Optional<KoreatechArticle> findById(Integer id);

    KoreatechArticle save(KoreatechArticle article);

    default KoreatechArticle getById(Integer articleId) {
        return findById(articleId).orElseThrow(/**/);
    }
}
