package in.koreatech.koin.domain.community.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.exception.ArticleNotFoundException;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;

    public ArticlesResponse getArticles(Long boardId, Long page, Long limit) {
        Board board = boardRepository.findById(boardId);

        PageRequest pageRequest = PageRequest.of(page.intValue() - 1, limit.intValue());
        Page<Article> articles = articleRepository.findByBoardId(boardId, pageRequest);
        if (articles.getContent().isEmpty()) {
            throw ArticleNotFoundException.withDetail("boardId: " + boardId + ", page: " + page + ", limit: " + limit);
        }

        return ArticlesResponse.of(articles.getContent(), board, (long)articles.getTotalPages());
    }
}
