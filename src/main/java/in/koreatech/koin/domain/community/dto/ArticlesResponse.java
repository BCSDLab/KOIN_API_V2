package in.koreatech.koin.domain.community.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import io.swagger.v3.oas.annotations.media.Schema;

public record ArticlesResponse(
    @Schema(description = "게시글 목록", requiredMode = NOT_REQUIRED)
    List<InnerArticleResponse> articles,

    @Schema(description = "게시판 정보", requiredMode = REQUIRED)
    InnerBoardResponse board,

    @Schema(description = "총 페이지 수", example = "1", requiredMode = REQUIRED)
    long totalPage
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

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerArticleResponse(

        @Schema(description = "게시글 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "게시판 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer boardId,

        @Schema(description = "제목", example = "제목", requiredMode = REQUIRED)
        String title,

        @Schema(description = "내용", example = "내용", requiredMode = REQUIRED)
        String content,

        @Schema(description = "작성자 고유 ID", example = "1", requiredMode = REQUIRED)
        int userId,

        @Schema(description = "작성자 닉네임", example = "닉네임", requiredMode = REQUIRED)
        String nickname,

        @Schema(description = "조회수", example = "1", requiredMode = REQUIRED)
        int hit,

        @Schema(description = "IP 주소", example = "123.12.1.3")
        String ip,

        @Schema(description = "해결 여부", example = "false")
        boolean isSolved,

        @Schema(description = "삭제 여부", example = "false")
        boolean isDeleted,

        @Schema(description = "댓글 수", example = "1")
        int commentCount,

        @Schema(description = "메타 정보", example = "메타 정보", requiredMode = NOT_REQUIRED)
        String meta,

        @Schema(description = "공지 여부", example = "false", requiredMode = REQUIRED)
        boolean isNotice,

        @Schema(description = "공지 게시글 고유 ID", example = "1", requiredMode = NOT_REQUIRED)
        Integer noticeArticleId,

        @Schema(description = "요약", example = "요약", requiredMode = NOT_REQUIRED)
        String summary,

        @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,

        @Schema(description = "내용 요약", example = "내용 요약", requiredMode = NOT_REQUIRED)
        @JsonProperty("contentSummary")
        String contentSummary
    ) {

        public static InnerArticleResponse from(Article article) {
            return new InnerArticleResponse(
                article.getId(),
                article.getBoard().getId(),
                article.getTitle(),
                article.getContent(),
                article.getUser().getId(),
                article.getNickname(),
                article.getHit(),
                article.getIp(),
                article.isSolved(),
                article.isDeleted(),
                article.getCommentCount(),
                article.getMeta(),
                article.isNotice(),
                article.getNoticeArticleId(),
                article.getSummary(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                article.getContentSummary()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerBoardResponse(
        @Schema(description = "게시판 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "게시판 태그", example = "notice", requiredMode = NOT_REQUIRED)
        String tag,

        @Schema(description = "게시판 명", example = "공지사항", requiredMode = REQUIRED)
        String name,

        @Schema(description = "익명 여부", example = "false", requiredMode = REQUIRED)
        boolean isAnonymous,

        @Schema(description = "게시글 수", example = "1", requiredMode = REQUIRED)
        int articleCount,

        @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
        boolean isDeleted,

        @Schema(description = "공지 여부", example = "false", requiredMode = REQUIRED)
        boolean isNotice,

        @Schema(description = "부모 게시판 고유 ID", example = "1", requiredMode = NOT_REQUIRED)
        Integer parentId,

        @Schema(description = "순서", example = "1", requiredMode = REQUIRED)
        int seq,

        @Schema(description = "하위 게시판 목록", requiredMode = NOT_REQUIRED)
        List<InnerBoardResponse> children,

        @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
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
                    ? null : board.getChildren().stream()
                    .map(InnerBoardResponse::from)
                    .toList(),
                board.getCreatedAt(),
                board.getUpdatedAt()
            );
        }
    }
}
