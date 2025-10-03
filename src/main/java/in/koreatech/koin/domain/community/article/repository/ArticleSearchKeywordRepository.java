package in.koreatech.koin.domain.community.article.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.ArticleSearchKeyword;
import in.koreatech.koin.global.config.repository.JpaRepository;
import jakarta.persistence.LockModeType;

@JpaRepository
public interface ArticleSearchKeywordRepository extends Repository<ArticleSearchKeyword, Integer> {

    ArticleSearchKeyword save(ArticleSearchKeyword keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ArticleSearchKeyword> findByKeyword(String keywordStr);

    @Query("""
        SELECT k.keyword
        FROM ArticleSearchKeyword k
        WHERE k.lastSearchedAt >= :fromDate
        ORDER BY k.weight DESC, k.lastSearchedAt DESC
        """)
    List<String> findTopKeywords(LocalDateTime fromDate, Pageable pageable);

    @Query("""
        SELECT k.keyword
        FROM ArticleSearchKeyword k
        ORDER BY k.totalSearch DESC, k.lastSearchedAt
        """)
    List<String> findTopKeywordsByLatest(Pageable pageable);

    List<ArticleSearchKeyword> findByUpdatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
}
