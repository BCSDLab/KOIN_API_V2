package in.koreatech.koin.domain.club.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.ClubQna;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record QnasResponse(
    @Schema(example = "10", description = "해당 동아리의 루트 질문 총 개수", requiredMode = REQUIRED)
    Integer rootCount,

    @Schema(example = "30", description = "해당 동아리의 qna 총 개수", requiredMode = REQUIRED)
    Integer totalCount,

    @Schema(description = "해당 동아리의 qna 정보 리스트")
    List<InnerQnaResponse> qnas
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerQnaResponse(
        @Schema(description = "qna ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "작성자 ID", example = "5400", requiredMode = REQUIRED)
        Integer authorId,

        @Schema(description = "작성자 닉네임", example = "김성현", requiredMode = REQUIRED)
        String nickname,

        @Schema(description = "내용", example = "언제 모집하나요?", requiredMode = REQUIRED)
        String content,

        @Schema(description = "삭제여부", example = "false", requiredMode = REQUIRED)
        Boolean isDeleted,

        @Schema(description = "관리자여부", example = "true", requiredMode = REQUIRED)
        Boolean isAdmin,

        @Schema(description = "작성 일시", example = "2025.05.12 14:00", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy.MM.dd HH:mm")
        LocalDateTime createdAt,

        @Schema(description = "해당 qna의 대댓글 정보 리스트")
        List<InnerQnaResponse> children
    ) {

        public static InnerQnaResponse from(ClubQna qna) {
            String nickname = qna.getAuthor().getUser().getNickname();
            if (nickname == null) {
                nickname = qna.getAuthor().getAnonymousNickname();
            }
            List<InnerQnaResponse> children = qna.getChildren().stream()
                .map(InnerQnaResponse::from)
                .toList();
            return new InnerQnaResponse(
                qna.getId(),
                qna.getAuthor().getId(),
                nickname,
                qna.getContent(),
                qna.getIsDeleted(),
                qna.getIsAdmin(),
                qna.getCreatedAt(),
                children
            );
        }
    }

    public static QnasResponse from(List<ClubQna> qnas) {
        List<InnerQnaResponse> roots = qnas.stream()
            .filter(ClubQna::isRoot)
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .map(InnerQnaResponse::from)
            .toList();

        return new QnasResponse(roots.size(), qnas.size(), roots);
    }
}
