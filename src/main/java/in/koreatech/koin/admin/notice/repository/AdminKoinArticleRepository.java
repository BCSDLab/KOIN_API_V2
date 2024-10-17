package in.koreatech.koin.admin.notice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.KoinArticle;

public interface AdminKoinArticleRepository extends Repository<KoinArticle, Integer> {

    @Query(value = "SELECT * FROM new_koin_articles WHERE article_id = :noticeId", nativeQuery = true)
    Optional<KoinArticle> findByArticleId(Integer noticeId);
}
