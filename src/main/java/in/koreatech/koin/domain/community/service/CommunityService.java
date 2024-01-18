package in.koreatech.koin.domain.community.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.dto.ArticleResponse;
import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.model.Criteria;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;

    public static final Sort SORT_ORDER_BY = Sort.by(Sort.Direction.DESC, "id");

    public ArticleResponse getArticle(Long id, String token) {
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> ArticleNotFoundException.withDetail("id: " + id));

        return null;
    }

    public ArticlesResponse getArticles(Long boardId, Long page, Long limit) {
        Criteria criteria = Criteria.of(page, limit);
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> ArticleNotFoundException.withDetail(
                "boardId: " + boardId + ", page: " + criteria.getPage() + ", limit: " + criteria.getLimit()));
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), SORT_ORDER_BY);
        Page<Article> articles = articleRepository.findByBoardId(boardId, pageRequest);
        if (articles.getContent().isEmpty()) {
            throw ArticleNotFoundException.withDetail(
                "boardId: " + boardId + ", page: " + criteria.getPage() + ", limit: " + criteria.getLimit());
        }

        return ArticlesResponse.of(articles.getContent(), board, (long)articles.getTotalPages());
    }
}
