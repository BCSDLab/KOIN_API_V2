package in.koreatech.koin.domain.community.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.ArticleViewLog;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.model.Criteria;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.ArticleViewLogRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    public static final Sort SORT_ORDER_BY = Sort.by(Sort.Direction.DESC, "id");

    private final ArticleRepository articleRepository;
    private final ArticleViewLogRepository articleViewLogRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public ArticleResponse getArticle(Long userId, Long articleId, String ipAddress) {
        Article article = articleRepository.getById(articleId);
        if (isHittable(articleId, userId, ipAddress)) {
            article.increaseHit();
        }
        article.getComment().forEach(comment -> comment.updateAuthority(userId));
        return ArticleResponse.of(article);
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
                    .article(articleRepository.getById(articleId))
                    .user(userRepository.getById(userId))
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

    public ArticlesResponse getArticles(Long boardId, Long page, Long limit) {
        Criteria criteria = Criteria.of(page, limit);
        Board board = boardRepository.getById(boardId);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), SORT_ORDER_BY);
        Page<Article> articles = articleRepository.getByBoardId(boardId, pageRequest);
        return ArticlesResponse.of(articles.getContent(), board, (long) articles.getTotalPages());
    }
}
