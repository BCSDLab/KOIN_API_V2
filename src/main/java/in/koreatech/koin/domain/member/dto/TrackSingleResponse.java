package in.koreatech.koin.domain.member.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin.domain.member.model.TechStack;
import in.koreatech.koin.domain.member.model.Track;
import io.swagger.v3.oas.annotations.media.Schema;

public record TrackSingleResponse(
    @JsonProperty("TrackName")
    @Schema(description = "트랙 명", example = "Backend", requiredMode = REQUIRED)
    String trackName,

    @JsonProperty("TechStacks")
    List<InnerTechStackResponse> innerTechStackResponses,

    @JsonProperty("Members")
    List<InnerMemberResponse> innerMemberResponses
) {

    public static TrackSingleResponse of(Track track, List<Member> members, List<TechStack> techStacks) {
        return new TrackSingleResponse(
            track.getName(),
            techStacks.stream()
                .map(InnerTechStackResponse::from)
                .toList(),
            members.stream()
                .map(member -> InnerMemberResponse.from(member, track.getName()))
                .toList()
        );
    }

    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    private record InnerTechStackResponse(
        @Schema(description = "기술 스택 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "기술 이름", example = "Backend")
        String name,

        @Schema(description = "기술 설명", example = "15")
        String description,

        @Schema(description = "이미지 Url", example = "https://static.koreatech.in/example/image.png")
        String imageUrl,

        @Schema(description = "트랙 ID", example = "1")
        Integer trackId,

        @Schema(description = "삭제 여부", example = "false")
        Boolean isDeleted,

        @Schema(description = "생성 일자", example = "2023-01-04 12:00:01")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {

        public static InnerTechStackResponse from(TechStack techStack) {
            return new InnerTechStackResponse(
                techStack.getId(),
                techStack.getName(),
                techStack.getDescription(),
                techStack.getImageUrl(),
                techStack.getTrackId(),
                techStack.isDeleted(),
                techStack.getCreatedAt(),
                techStack.getUpdatedAt()
            );
        }
    }

    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    private record InnerMemberResponse(
        @Schema(description = "BCSD 회원 고유 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이름", example = "최준호", requiredMode = REQUIRED)
        String name,

        @Schema(description = "학번", example = "2019136135", requiredMode = NOT_REQUIRED)
        String studentNumber,

        @Schema(description = "동아리 포지션 `Beginner`, `Regular`, `Mentor`", example = "Regular", requiredMode = REQUIRED)
        String position,

        @Schema(description = "트랙 명", example = "Backend", requiredMode = REQUIRED)
        String track,

        @Schema(description = "이메일", example = "koin123@koreatech.ac.kr", requiredMode = NOT_REQUIRED)
        String email,

        @Schema(description = "이미지 Url", example = "https://static.koreatech.in/example/image.png", requiredMode = NOT_REQUIRED)
        String imageUrl,

        @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
        Boolean isDeleted,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "생성 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "수정 일자", example = "2023-01-04 12:00:01", requiredMode = REQUIRED)
        LocalDateTime updatedAt
    ) {

        public static InnerMemberResponse from(Member member, String trackName) {
            return new InnerMemberResponse(
                member.getId(),
                member.getName(),
                member.getStudentNumber(),
                member.getPosition(),
                trackName,
                member.getEmail(),
                member.getImageUrl(),
                member.isDeleted(),
                member.getCreatedAt(),
                member.getUpdatedAt()
            );
        }
    }
}
