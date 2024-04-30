package in.koreatech.koin.domain.community.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.community.model.Comment;

public interface CommentRepository extends Repository<Comment, Integer> {

    List<Comment> findAllByArticleId(Integer articleId);

    Comment save(Comment comment);
}
