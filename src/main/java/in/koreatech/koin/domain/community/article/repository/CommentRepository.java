package in.koreatech.koin.domain.community.article.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.article.model.Comment;

public interface CommentRepository extends Repository<Comment, Integer> {

    List<Comment> findAllByArticleId(Integer articleId);

    Optional<Comment> findByIdAndArticle_Id(Integer id, Integer articleId);

    Comment save(Comment comment);
}
