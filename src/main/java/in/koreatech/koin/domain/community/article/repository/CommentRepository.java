package in.koreatech.koin.domain.community.article.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.Comment;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface CommentRepository extends Repository<Comment, Integer> {

    List<Comment> findAllByArticleId(Integer articleId);

    Comment save(Comment comment);
}
