package in.koreatech.koin.domain.community.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;

    public ArticlesResponse getArticles(Long boardId, Long page, Long limit) {
        page -= 1;
        Board board = boardRepository.findById(boardId)
            .orElseThrow(); //TODO: 404 Not Found Handling

        PageRequest pageRequest = PageRequest.of(page.intValue(), limit.intValue());
        Page<Article> articles = articleRepository.findByBoardId(boardId, pageRequest);
        // Article article = articleRepository.findAllByBoardId(boardId)
        //     .orElseThrow(); //TODO: 404 Not Found Handling - errorResponse, "There is no article"

        return ArticlesResponse.of(articles.getContent(), board, (long)articles.getTotalPages());
    }
}
