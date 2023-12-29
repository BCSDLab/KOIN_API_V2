package in.koreatech.koin.domain.track.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.track.model.Member;
import in.koreatech.koin.domain.track.model.TechStack;
import in.koreatech.koin.domain.track.model.Track;

public record TrackSingleResponse(
    @JsonProperty("TrackName") String trackName,
    @JsonProperty("TechStacks") List<InnerTechStackResponse> innerTechStackResponses,
    @JsonProperty("Members") List<InnerMemberResponse> innerMemberResponses
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
        Long id,
        String name,
        String description,
        String imageUrl,
        Long trackId,
        Boolean isDeleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
    ) {

        public static InnerTechStackResponse from(TechStack techStack) {
            return new InnerTechStackResponse(
                techStack.getId(),
                techStack.getName(),
                techStack.getDescription(),
                techStack.getImageUrl(),
                techStack.getTrackId(),
                techStack.getIsDeleted(),
                techStack.getCreatedAt(),
                techStack.getUpdatedAt()
            );
        }
    }

    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    private record InnerMemberResponse(
        Long id,
        String name,
        String studentNumber,
        String position,
        String track,
        String email,
        String imageUrl,
        Boolean isDeleted,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt
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
                member.getIsDeleted(),
                member.getCreatedAt(),
                member.getUpdatedAt()
            );
        }
    }
}
