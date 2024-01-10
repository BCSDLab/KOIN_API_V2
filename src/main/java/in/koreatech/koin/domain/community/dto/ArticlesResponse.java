package in.koreatech.koin.domain.community.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;

public record ArticlesResponse(
    List<InnerArticleResponse> articles,
    InnerBoardResponse board,
    Long totalPage
) {

    public static ArticlesResponse of(List<Article> articles, Board board, Long totalPage) {
        return new ArticlesResponse(
            articles.stream()
                .map(InnerArticleResponse::from)
                .toList(),
            InnerBoardResponse.from(board),
            totalPage
        );
    }

    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    private record InnerArticleResponse(
        Long id,
        Long boardId,
        String title,
        String content,
        Long userId,
        String nickname,
        Long hit, String ip,
        Boolean isSolved,
        Boolean isDeleted,
        Byte commentCount,
        String meta,
        Long noticeArticleId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {

        public static InnerArticleResponse from(Article article) {
            return new InnerArticleResponse(
                article.getId(),
                article.getBoardId(),
                article.getTitle(),
                article.getContent(),
                article.getUserId(),
                article.getNickname(),
                article.getHit(),
                article.getIp(),
                article.getIsSolved(),
                article.getIsDeleted(),
                article.getCommentCount(),
                article.getMeta(),
                article.getNoticeArticleId(),
                article.getCreatedAt(),
                article.getUpdatedAt()
            );
        }
    }

    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static record InnerBoardResponse(
        Long id,
        String tag,
        String name,
        Boolean isAnonymous,
        Long articleCount,
        Boolean isDeleted,
        Long parentId,
        Long seq,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {

        public static InnerBoardResponse from(Board board) {
            return new InnerBoardResponse(
                board.getId(),
                board.getTag(),
                board.getName(),
                board.getIsAnonymous(),
                board.getArticleCount(),
                board.getIsDeleted(),
                board.getParentId(),
                board.getSeq(),
                board.getCreatedAt(),
                board.getUpdatedAt()
            );
        }
    }
}
