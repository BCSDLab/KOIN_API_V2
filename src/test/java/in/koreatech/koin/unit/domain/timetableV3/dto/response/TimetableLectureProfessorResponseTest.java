package in.koreatech.koin.unit.domain.timetableV3.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.dto.response.TakeAllTimetableLectureResponse;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;

@SuppressWarnings("NonAsciiCharacters")
class TimetableLectureProfessorResponseTest {

    @Test
    void 강의와_시간표_강의의_교수가_모두_없어도_교수는_null로_응답한다() {
        TimetableLecture timetableLecture = timetableLecture(lecture(null), null);

        var response = TimetableLectureResponseV3.InnerTimetableLectureResponseV3
            .from(List.of(timetableLecture));

        assertThat(response).hasSize(1);
        assertThat(response.get(0).professor()).isNull();
    }

    @Test
    void 시간표_강의의_교수가_없으면_강의의_교수로_응답한다() {
        TimetableLecture timetableLecture = timetableLecture(lecture("허준기"), null);

        var response = TimetableLectureResponseV3.InnerTimetableLectureResponseV3
            .from(List.of(timetableLecture));

        assertThat(response.get(0).professor()).isEqualTo("허준기");
    }

    @Test
    void 시간표_강의에_교수가_있으면_강의의_교수보다_우선한다() {
        TimetableLecture timetableLecture = timetableLecture(lecture("허준기"), "수정된 교수");

        var response = TimetableLectureResponseV3.InnerTimetableLectureResponseV3
            .from(List.of(timetableLecture));

        assertThat(response.get(0).professor()).isEqualTo("수정된 교수");
    }

    @Test
    void 이수한_전체_수업_응답도_교수가_모두_없으면_null로_응답한다() {
        TimetableLecture timetableLecture = timetableLecture(lecture(null), null);

        var response = TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3
            .from(List.of(timetableLecture));

        assertThat(response).hasSize(1);
        assertThat(response.get(0).professor()).isNull();
    }

    @Test
    void 이수한_전체_수업_응답은_시간표_강의의_교수가_없으면_강의의_교수로_응답한다() {
        TimetableLecture timetableLecture = timetableLecture(lecture("허준기"), null);

        var response = TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3
            .from(List.of(timetableLecture));

        assertThat(response.get(0).professor()).isEqualTo("허준기");
    }

    private Lecture lecture(String professor) {
        return Lecture.builder()
            .code("MEB311")
            .semester("20192")
            .name("재료역학")
            .grades("3")
            .lectureClass("01")
            .regularNumber("35")
            .department("기계공학부")
            .target("기공전체")
            .professor(professor)
            .isEnglish("")
            .designScore("0")
            .isElearning("")
            .classTime("[100, 101, 102, 103, 308, 309]")
            .build();
    }

    private TimetableLecture timetableLecture(Lecture lecture, String professor) {
        return TimetableLecture.builder()
            .lecture(lecture)
            .grades("0")
            .professor(professor)
            .build();
    }
}
