package in.koreatech.koin.domain.community.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import in.koreatech.koin.domain.auth.JwtProvider;
import in.koreatech.koin.domain.auth.exception.AuthException;
import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.ArticleViewLog;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.model.Comment;
import in.koreatech.koin.domain.community.model.Criteria;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.ArticleViewLogRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import in.koreatech.koin.domain.community.repository.CommentRepository;
import in.koreatech.koin.global.util.ClientUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private static final String AUTHORIZATION = "Authorization";
    public static final Sort SORT_ORDER_BY = Sort.by(Sort.Direction.DESC, "id");

    private final JwtProvider jwtProvider;
    private final ArticleRepository articleRepository;
    private final ArticleViewLogRepository articleViewLogRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public ArticleResponse getArticle(Long articleId) {
        Article article = articleRepository.getById(articleId);
        Board board = boardRepository.getById(article.getBoardId());
        Long userId = getUserId();
        String ipAddress = getIpAddress();
        if (isHittable(articleId, userId, ipAddress)) {
            article.increaseHit();
        }
        List<Comment> comments = commentRepository.findAllByArticleId(articleId);
        comments.forEach(comment -> comment.updateAuthority(userId));
        return ArticleResponse.of(article, board, comments);
    }

    private boolean isHittable(Long articleId, Long userId, String ipAddress) {
        if (userId == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        Optional<ArticleViewLog> foundLog = articleViewLogRepository.findByArticleIdAndUserId(articleId, userId);
        if (foundLog.isEmpty()) {
            articleViewLogRepository.save(
                ArticleViewLog.builder()
                    .articleId(articleId)
                    .userId(userId)
                    .ip(ipAddress)
                    .build()
            );
            return true;
        }
        if (now.isAfter(foundLog.get().getExpiredAt())) {
            foundLog.get().updateExpiredTime();
            return true;
        }
        return false;
    }

    private Long getUserId() {
        try {
            HttpServletRequest request =
                ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
            return jwtProvider.getUserId(request.getHeader(AUTHORIZATION));
        } catch (AuthException e) {
            return null;
        }
    }

    private String getIpAddress() {
        HttpServletRequest request =
            ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
        return ClientUtil.getClientIP(request);
    }

    public ArticlesResponse getArticles(Long boardId, Long page, Long limit) {
        Criteria criteria = Criteria.of(page, limit);
        Board board = boardRepository.getById(boardId);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), SORT_ORDER_BY);
        Page<Article> articles = articleRepository.getByBoardId(boardId, pageRequest);
        return ArticlesResponse.of(articles.getContent(), board, (long)articles.getTotalPages());
    }
}
