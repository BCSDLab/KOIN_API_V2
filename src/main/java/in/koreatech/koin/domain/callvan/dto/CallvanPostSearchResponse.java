package in.koreatech.koin.domain.callvan.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.CallvanPost;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CallvanPostSearchResponse(
    @Schema(description = "콜밴 게시글 목록")
    List<CallvanPostResponse> posts,

    @Schema(description = "총 게시글 수", example = "57")
    Long totalCount,

    @Schema(description = "현재 페이지 번호", example = "1")
    Integer currentPage,

    @Schema(description = "전체 페이지 수", example = "6")
    Integer totalPage
) {
    public static CallvanPostSearchResponse of(List<CallvanPost> posts, Long totalCount, Integer currentPage,
        Integer totalPage, Set<Integer> joinedPostIds, Integer userId) {
        return new CallvanPostSearchResponse(
            posts.stream()
                .map(post -> CallvanPostResponse.from(post, joinedPostIds.contains(post.getId()), userId))
                .toList(),
            totalCount,
            currentPage,
            totalPage
        );
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CallvanPostResponse(
        @Schema(description = "게시글 ID", example = "1")
        Integer id,

        @Schema(description = "제목", example = "복지관 -> 천안역")
        String title,

        @Schema(description = "출발지", example = "복지관")
        String departure,

        @Schema(description = "도착지", example = "천안역")
        String arrival,

        @Schema(description = "출발 날짜", example = "2024-03-24")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate departureDate,

        @Schema(description = "출발 시간", example = "14:00")
        @JsonFormat(pattern = "HH:mm")
        LocalTime departureTime,

        @Schema(description = "작성자 닉네임", example = "코인")
        String authorNickname,

        @Schema(description = "현재 모집 인원", example = "2")
        Integer currentParticipants,

        @Schema(description = "최대 모집 인원", example = "4")
        Integer maxParticipants,

        @Schema(description = "모집 상태", example = "RECRUITING")
        String status,

        @Schema(description = "참여 여부", example = "true")
        Boolean isJoined,

        @Schema(description = "작성자 여부", example = "true")
        Boolean isAuthor
    ) {
        public static CallvanPostResponse from(CallvanPost post, boolean isJoined, Integer userId) {
            String departureName = post.getDepartureType().getName();
            if (post.getDepartureCustomName() != null && !post.getDepartureCustomName().isBlank()) {
                departureName = post.getDepartureCustomName();
            }

            String arrivalName = post.getArrivalType().getName();
            if (post.getArrivalCustomName() != null && !post.getArrivalCustomName().isBlank()) {
                arrivalName = post.getArrivalCustomName();
            }

            return new CallvanPostResponse(
                post.getId(),
                post.getTitle(),
                departureName,
                arrivalName,
                post.getDepartureDate(),
                post.getDepartureTime(),
                post.getAuthor().getNickname(),
                post.getCurrentParticipants(),
                post.getMaxParticipants(),
                post.getStatus().name(),
                isJoined,
                post.getAuthor().getId().equals(userId));
        }
    }
}
