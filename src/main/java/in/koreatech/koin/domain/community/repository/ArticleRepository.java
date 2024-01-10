package in.koreatech.koin.domain.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.model.Article;

public interface ArticleRepository extends Repository<Article, Long> {

    Page<Article> findByBoardId(Long boardId, Pageable pageable);
}
