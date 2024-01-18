package in.koreatech.koin.domain.community.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.auth.JwtProvider;
import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.model.Comment;
import in.koreatech.koin.domain.community.model.Criteria;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import in.koreatech.koin.domain.community.repository.CommentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final JwtProvider jwtProvider;
    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public static final Sort SORT_ORDER_BY = Sort.by(Sort.Direction.DESC, "id");

    public ArticleResponse getArticle(Long articleId, String token) {
        Article article = articleRepository.getById(articleId);
        Board board = boardRepository.getById(article.getBoardId());
        List<Comment> comments = commentRepository.findAllByArticleId(articleId);
        comments.forEach(comment -> comment.updateAuthority(jwtProvider.getUserId(token)));
        return ArticleResponse.of(article, board, comments);
    }

    public ArticlesResponse getArticles(Long boardId, Long page, Long limit) {
        Criteria criteria = Criteria.of(page, limit);
        Board board = boardRepository.getById(boardId);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), SORT_ORDER_BY);
        Page<Article> articles = articleRepository.findByBoardId(boardId, pageRequest);
        return ArticlesResponse.of(articles.getContent(), board, (long)articles.getTotalPages());
    }
}
