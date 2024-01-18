package in.koreatech.koin.domain.community.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.model.Comment;

@JsonNaming(SnakeCaseStrategy.class)
public record ArticleResponse(
    Long id,
    Long board_id,
    String title,
    String content,
    String nickname,
    Boolean is_solved,
    Boolean is_notice,
    String contentSummary,
    Long hit,
    Long comment_count,
    InnerBoardResponse board,
    List<InnerCommentResponse> comments,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created_at,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updated_at
) {

    private record InnerBoardResponse(
        Long id,
        String tag,
        String name,
        Boolean is_anonymous,
        Long article_count,
        Boolean is_deleted,
        Boolean is_notice,
        Long parent_id,
        Long seq,
        List<InnerBoardResponse> children,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created_at,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updated_at
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

    private record InnerCommentResponse(
        Long id,
        Long article_id,
        String content,
        Long user_id,
        String nickname,
        Boolean is_deleted,
        Boolean grantEdit,
        Boolean grantDelete,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created_at,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updated_at
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
