package in.koreatech.koin.domain.timetable.dto;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TimeTableRequest (
    @Schema(description = "시간표 정보", example = """
        [
        "class_title" : "운영체제",
        "class_time" : "[210, 211]",
        "grades" : "3"
        ]
        """)
    @Valid
    @NotNull(message = "시간표 정보를 입력해주세요.")
    List<InnerTimeTableRequest> timetable,

    @Schema(description = "학기 정보", example = "20192")
    @NotBlank(message = "학기 정보를 입력해주세요.")
    String semester
){
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record InnerTimeTableRequest(
        @Schema(description = "과목 코드", example = "CPC490")
        String code,

        @Schema(description = "강의 이름", example = "운영체제")
        @NotBlank(message = "강의 이름을 입력해주세요.")
        String classTitle,

        @Schema(description = "강의 시간", example = "[210, 211]")
        @NotNull(message = "강의 시간을 입력해주세요.")
        List<Integer> classTime,

        @Schema(description = "강의 장소", example = "null")
        String classPlace,

        @Schema(name = "강의 교수", example = "이돈우")
        String professor,

        @Schema(description = "대상 학년", example = "3")
        @NotBlank(message = "대상 학년을 입력해주세요.")
        String grades,

        @Schema(name = "분반", example = "01")
        String lectureClass,

        @Schema(name = "대상", example = "디자 1 건축")
        String target,

        @Schema(name = "수강 인원", example = "25")
        String regularNumber,

        @Schema(name = "설계 학점", example = "0")
        String designScore,

        @Schema(name = "학부", example = "디자인ㆍ건축공학부")
        String department,

        @Schema(name = "memo", example = "null")
        String memo
    ){

    }
    public static TimeTable toEntity(User user, Semester semester, InnerTimeTableRequest request){
        return TimeTable.builder()
            .user(user)
            .semester(semester)
            .code(request.code())
            .classTitle(request.classTitle())
            .classTime(Arrays.toString(request.classTime().stream().toArray()))
            .classPlace(request.classPlace())
            .professor(request.professor())
            .grades(request.grades())
            .lectureClass(request.lectureClass())
            .target(request.target())
            .regularNumber(request.regularNumber())
            .designScore(request.designScore())
            .department(request.department())
            .memo(request.memo())
            .isDeleted(false)
            .build();
    }
}
