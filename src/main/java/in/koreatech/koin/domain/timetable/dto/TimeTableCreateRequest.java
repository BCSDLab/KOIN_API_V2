package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimeTableCreateRequest(
    @Valid
    @Schema(description = "시간표 정보", requiredMode = REQUIRED)
    @NotNull(message = "시간표 정보를 입력해주세요.")
    List<InnerTimeTableRequest> timetable,

    @Schema(description = "학기 정보", example = "20192", requiredMode = REQUIRED)
    @NotBlank(message = "학기 정보를 입력해주세요.")
    String semester
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimeTableRequest(
        @Schema(description = "과목 코드", example = "CPC490", requiredMode = NOT_REQUIRED)
        String code,

        @Schema(description = "강의(커스텀) 이름", example = "운영체제", requiredMode = REQUIRED)
        @NotBlank(message = "강의 이름을 입력해주세요.")
        String classTitle,

        @Schema(description = "강의(커스텀) 시간", example = "[210, 211]", requiredMode = REQUIRED)
        @NotNull(message = "강의 시간을 입력해주세요.")
        List<Integer> classTime,

        @Schema(description = "강의 장소", example = "2공학관", requiredMode = NOT_REQUIRED)
        String classPlace,

        @Schema(name = "강의 교수", example = "이돈우", requiredMode = NOT_REQUIRED)
        String professor,

        @Schema(description = "대상 학년", example = "3", requiredMode = NOT_REQUIRED)
        String grades,

        @Schema(name = "분반", example = "01", requiredMode = NOT_REQUIRED)
        @Size(max = 3, message = "분반은 3자 이하로 입력해주세요.")
        String lectureClass,

        @Schema(name = "대상", example = "디자 1 건축", requiredMode = NOT_REQUIRED)
        @Size(max = 200, message = "대상은 200자 이하로 입력해주세요.")
        String target,

        @Schema(name = "수강 인원", example = "25", requiredMode = NOT_REQUIRED)
        @Size(max = 4, message = "수강 인원은 4자 이하로 입력해주세요.")
        String regularNumber,

        @Schema(name = "설계 학점", example = "0", requiredMode = NOT_REQUIRED)
        @Size(max = 4, message = "설계 학점은 4자 이하로 입력해주세요.")
        String designScore,

        @Schema(name = "학부", example = "디자인ㆍ건축공학부", requiredMode = NOT_REQUIRED)
        @Size(max = 30, message = "학부는 30자 이하로 입력해주세요.")
        String department,

        @Schema(name = "memo", example = "메모메모", requiredMode = NOT_REQUIRED)
        @Size(max = 200, message = "메모는 200자 이하로 입력해주세요.")
        String memo
    ) {

        public TimeTable toTimeTable(User user, Semester semester) {
            return TimeTable.builder()
                .user(user)
                .semester(semester)
                .code(this.code)
                .classTitle(this.classTitle())
                .classTime(Arrays.toString(this.classTime().stream().toArray()))
                .classPlace(this.classPlace())
                .professor(this.professor())
                .grades(this.grades())
                .lectureClass(this.lectureClass())
                .target(this.target())
                .regularNumber(this.regularNumber())
                .designScore(this.designScore())
                .department(this.department())
                .memo(this.memo())
                .isDeleted(false)
                .build();
        }
    }
}
