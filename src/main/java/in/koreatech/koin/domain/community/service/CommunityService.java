package in.koreatech.koin.domain.community.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.community.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;

    public ArticlesResponse getArticles(Long boardId, Long page, Long limit) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(); //TODO: 404 Not Found Handling
        Article article = articleRepository.findByBoardId(boardId)
            .orElse(null);

        return null;
    }
}
