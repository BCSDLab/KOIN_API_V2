package in.koreatech.koin.domain.member.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record MemberResponse(
    @Schema(example = "1")
    Long id,

    @Schema(example = "최준호")
    String name,

    @Schema(example = "2019136135")
    String studentNumber,

    @Schema(example = "Backend")
    String track,

    @Schema(example = "Regular")
    String position,

    @Schema(example = "juno@gmail.com")
    String email,

    @Schema(example = "https://static.koreatech.in/bcsdlab_page_assets/img/people/juno.jpg")
    String imageUrl,

    @Schema(example = "false")
    Boolean isDeleted,

    @Schema(example = "2020-08-14 16:26:35")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(example = "2021-08-16 06:42:44")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getName(),
            member.getStudentNumber(),
            member.getTrack().getName(),
            member.getPosition(),
            member.getEmail(),
            member.getImageUrl(),
            member.getIsDeleted(),
            member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }
}
