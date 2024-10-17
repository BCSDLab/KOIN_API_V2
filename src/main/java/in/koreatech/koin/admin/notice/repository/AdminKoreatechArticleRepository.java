package in.koreatech.koin.admin.notice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.KoreatechArticle;

public interface AdminKoreatechArticleRepository extends Repository<KoreatechArticle, Integer> {

    @Query(value = "SELECT * FROM new_koreatech_articles WHERE article_id = :noticeId", nativeQuery = true)
    Optional<KoreatechArticle> findByArticleId(Integer noticeId);
}
