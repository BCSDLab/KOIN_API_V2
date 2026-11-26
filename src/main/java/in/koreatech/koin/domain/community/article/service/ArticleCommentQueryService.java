package in.koreatech.koin.domain.community.article.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.dto.ArticleCommentResponse;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.CommentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleCommentQueryService {
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    public List<ArticleCommentResponse> getComments(Integer articleId) {
        articleRepository.getById(articleId);
        return commentRepository.findAllByArticleId(articleId).stream().map(ArticleCommentResponse::from).toList();
    }
}
