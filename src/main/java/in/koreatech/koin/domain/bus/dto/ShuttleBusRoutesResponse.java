package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "셔틀버스 경로 응답")
public record ShuttleBusRoutesResponse(
    @Schema(description = "노선 지역 분류 목록")
    List<RouteRegion> routeRegions,

    @Schema(description = "학기 정보")
    RouteSemester semesterInfo
) {

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "노선 지역 정보")
    public record RouteRegion(
        @Schema(description = "지역 이름", example = "천안")
        String region,

        @Schema(description = "해당 지역의 경로 목록")
        List<RouteName> routes
    ) implements Comparable<RouteRegion> {

        @Override
        public int compareTo(RouteRegion routeName) {
            return Integer.compare(getPriority(this.region), getPriority(routeName.region));
        }

        private int getPriority(String region) {
            return switch (region) {
                case "천안" -> 1;
                case "청주" -> 2;
                case "서울" -> 3;
                default -> 4;
            };
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "노선 세부 정보")
    public record RouteName(
        @Schema(description = "노선 ID", example = "675013f9465776d6265ddfdb") String id,
        @Schema(description = "노선 종류", example = "주말") String type,
        @Schema(description = "노선 이름", example = "대학원") String routeName,
        @Schema(description = "노선 부가 이름", example = "토요일") String subName
    ) implements Comparable<RouteName> {

        @Override
        public int compareTo(RouteName routeName) {
            return Integer.compare(getPriority(this.type), getPriority(routeName.type));
        }

        private int getPriority(String type) {
            return switch (type) {
                case "순환" -> 1;
                case "주중" -> 2;
                case "주말" -> 3;
                default -> 4;
            };
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    @Schema(description = "학기 정보")
    public record RouteSemester(
        @Schema(description = "학기 이름", example = "정규학기") String name,
        @Schema(description = "학기 기간", example = "2024-09-02 ~ 2024-12-20") String term
    ) {
    }
}
