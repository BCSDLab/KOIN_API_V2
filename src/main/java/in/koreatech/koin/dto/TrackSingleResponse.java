package in.koreatech.koin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import in.koreatech.koin.domain.Member;
import in.koreatech.koin.domain.TechStack;
import in.koreatech.koin.domain.Track;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TrackSingleResponse {

    @JsonProperty("TrackName")
    private String trackName;

    @JsonProperty("TechStacks")
    private List<InnerTechStackResponse> innerTechStackResponses;

    @JsonProperty("Members")
    private List<InnerMemberResponse> innerMemberResponses;

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

    @Getter
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class InnerTechStackResponse {

        private Long id;
        private String name;
        private String description;
        private String imageUrl;
        private Long trackId;
        private Boolean isDeleted;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

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

    @Getter
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class InnerMemberResponse {

        private Integer id;
        private String name;
        private String studentNumber;
        private String position;
        private String track;
        private String email;
        private String imageUrl;
        private Boolean isDeleted;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

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
