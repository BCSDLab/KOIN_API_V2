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
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ArticleResponse(
    @Schema(description = "게시글 고유 ID", example = "1")
    Integer id,

    @Schema(description = "게시판 고유 ID", example = "1")
    Integer boardId,

    @Schema(description = "제목", example = "제목")
    String title,

    @Schema(description = "내용", example = "내용")
    String content,

    @Schema(description = "작성자 닉네임", example = "닉네임")
    String nickname,

    @Schema(description = "해결 여부", example = "false")
    Boolean isSolved,

    @Schema(description = "공지 여부", example = "false")
    Boolean isNotice,

    @Schema(description = "내용 요약", example = "내용 요약")
    @JsonProperty("contentSummary") String contentSummary,

    @Schema(description = "조회수", example = "1")
    Integer hit,

    @Schema(description = "댓글 수", example = "1")
    Byte commentCount,

    @Schema(description = "게시판 정보")
    InnerBoardResponse board,

    @Schema(description = "댓글 목록")
    List<InnerCommentResponse> comments,

    @Schema(description = "생성 일자", example = "2023-01-04 12:00:01")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,

    @Schema(description = "수정 일자", example = "2023-01-04 12:00:01")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
) {

    public static ArticleResponse of(Article article) {
        return new ArticleResponse(
            article.getId(),
            article.getBoard().getId(),
            article.getTitle(),
            article.getContent(),
            article.getNickname(),
            article.isSolved(),
            article.isNotice(),
            article.getContentSummary(),
            article.getHit(),
            article.getCommentCount(),
            InnerBoardResponse.from(article.getBoard()),
            article.getComment().stream().map(InnerCommentResponse::from).toList(),
            article.getCreatedAt(),
            article.getUpdatedAt()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerBoardResponse(
        @Schema(description = "게시판 고유 ID", example = "1")
        Integer id,

        @Schema(description = "게시판 태그", example = "tag")
        String tag,

        @Schema(description = "게시판 이름", example = "게시판 이름")
        String name,

        @Schema(description = "익명 여부", example = "false")
        boolean isAnonymous,

        @Schema(description = "게시글 수", example = "1")
        Integer articleCount,

        @Schema(description = "삭제 여부", example = "false")
        Boolean isDeleted,

        @Schema(description = "공지 여부", example = "false")
        Boolean isNotice,

        @Schema(description = "부모 게시판 고유 ID", example = "1")
        Long parentId,

        @Schema(description = "순서", example = "1")
        Long seq,

        @Schema(description = "하위 게시판 목록")
        List<InnerBoardResponse> children,

        @Schema(description = "생성 일자", example = "2023-01-04 12:00:01")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {

        public static InnerBoardResponse from(Board board) {
            return new InnerBoardResponse(
                board.getId(),
                board.getTag(),
                board.getName(),
                board.getIsAnonymous(),
                board.getArticleCount(),
                board.isDeleted(),
                board.isNotice(),
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
        @Schema(description = "댓글 고유 ID", example = "1")
        Long id,

        @Schema(description = "게시글 고유 ID", example = "1")
        Integer articleId,

        @Schema(description = "내용", example = "내용")
        String content,

        @Schema(description = "작성자 고유 ID", example = "1")
        Integer userId,

        @Schema(description = "작성자 닉네임", example = "닉네임")
        String nickname,

        @Schema(description = "삭제 여부", example = "false")
        Boolean isDeleted,

        @Schema(description = "수정 권한", example = "false")
        @JsonProperty("grantEdit") Boolean grantEdit,

        @Schema(description = "삭제 권한", example = "false")
        @JsonProperty("grantDelete") Boolean grantDelete,

        @Schema(description = "생성 일자", example = "2023-01-04 12:00:01")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {

        public static InnerCommentResponse from(Comment comment) {
            return new InnerCommentResponse(
                comment.getId(),
                comment.getArticle().getId(),
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
