package in.koreatech.koin.domain.course_registration.service;

import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserOwnsFrame;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.course_registration.dto.CourseRegistrationLectureResponse;
import in.koreatech.koin.domain.course_registration.dto.LectureSearchCriteria;
import in.koreatech.koin.domain.course_registration.dto.PreCourseRegistrationLectureResponse;
import in.koreatech.koin.domain.course_registration.repository.CourseCustomRepository;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV3.repository.TimetableFrameRepositoryV3;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRegistrationService {

    private final TimetableFrameRepositoryV3 timetableFrameRepositoryV3;
    private final CourseCustomRepository courseCustomRepository;

    public List<PreCourseRegistrationLectureResponse> getUserPreRegistrationCourses(Integer timetableFrameId,
        Integer userId) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(timetableFrameId);
        validateUserOwnsFrame(frame.getUser().getId(), userId);

        return PreCourseRegistrationLectureResponse.fromList(frame.getTimetableLectures());
    }

    public List<CourseRegistrationLectureResponse> searchCourseRegistrationLecture(String name, String department, Integer year, String semester) {
        LectureSearchCriteria criteria = new LectureSearchCriteria(name, department, year, semester);
        List<Lecture> lectures = courseCustomRepository.searchLectures(criteria);

        return CourseRegistrationLectureResponse.fromList(lectures);
    }
}
