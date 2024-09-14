package in.koreatech.koin.domain.community.article.repository.redis;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import in.koreatech.koin.domain.community.article.model.redis.ArticleHit;

public interface ArticleHitRepository extends CrudRepository<ArticleHit, Integer> {

    List<ArticleHit> findAll();
}
