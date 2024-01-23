package in.koreatech.koin.domain.community.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.model.Comment;

@JsonNaming(SnakeCaseStrategy.class)
public record ArticleResponse(
    Long id,
    Long boardId,
    String title,
    String content,
    String nickname,
    Boolean isSolved,
    Boolean isNotice,
    @JsonProperty("contentSummary") String contentSummary,
    Long hit,
    Byte commentCount,
    InnerBoardResponse board,
    List<InnerCommentResponse> comments,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
) {

    public static ArticleResponse of(Article article, Board board, List<Comment> comments) {
        return new ArticleResponse(
            article.getId(),
            article.getBoardId(),
            article.getTitle(),
            article.getContent(),
            article.getNickname(),
            article.getIsSolved(),
            article.getIsNotice(),
            article.getContentSummary(),
            article.getHit(),
            article.getCommentCount(),
            InnerBoardResponse.from(board),
            comments.stream().map(InnerCommentResponse::from).toList(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerBoardResponse(
        Long id,
        String tag,
        String name,
        Boolean isAnonymous,
        Long articleCount,
        Boolean isDeleted,
        Boolean isNotice,
        Long parentId,
        Long seq,
        List<InnerBoardResponse> children,
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
                board.getIsNotice(),
                board.getParentId(),
                board.getSeq(),
                board.getChildren().isEmpty()
                    ? null : board.getChildren().stream().map(InnerBoardResponse::from).toList(),
                board.getCreatedAt(),
                board.getUpdatedAt()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerCommentResponse(
        Long id,
        Long articleId,
        String content,
        Long userId,
        String nickname,
        Boolean isDeleted,
        @JsonProperty("grantEdit") Boolean grantEdit,
        @JsonProperty("grantDelete") Boolean grantDelete,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {

        public static InnerCommentResponse from(Comment comment) {
            return new InnerCommentResponse(
                comment.getId(),
                comment.getArticleId(),
                comment.getContent(),
                comment.getUserId(),
                comment.getNickname(),
                comment.getIsDeleted(),
                comment.getGrantEdit(),
                comment.getGrantDelete(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
            );
        }
    }
}
