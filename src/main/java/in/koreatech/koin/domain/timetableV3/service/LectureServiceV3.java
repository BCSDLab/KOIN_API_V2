package in.koreatech.koin.domain.timetableV3.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV3.dto.response.LectureResponseV3;
import in.koreatech.koin.domain.timetableV3.model.LectureInformation;
import in.koreatech.koin.domain.timetableV3.repository.LectureInformationRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.LectureRepositoryV3;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureServiceV3 {

    private final LectureInformationRepositoryV3 lectureInformationRepositoryV3;
    private final LectureRepositoryV3 lectureRepositoryV3;

    public List<LectureResponseV3> getLectures(String semesterDate) {
        List<Lecture> lectures = lectureRepositoryV3.findBySemester(semesterDate);
        List<LectureResponseV3> response = new ArrayList<>();

        for (Lecture lecture : lectures) {
            List<LectureInformation> lectureInformations = lectureInformationRepositoryV3.findByLectureId(
                lecture.getId());
            response.add(LectureResponseV3.from(lecture, lectureInformations));
        }

        return response;
    }
}
