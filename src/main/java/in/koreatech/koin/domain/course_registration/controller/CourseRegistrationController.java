package in.koreatech.koin.domain.course_registration.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.course_registration.dto.CourseRegistrationLectureResponse;
import in.koreatech.koin.domain.course_registration.dto.PreCourseRegistrationLectureResponse;
import in.koreatech.koin.domain.course_registration.service.CourseRegistrationService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CourseRegistrationController implements CourseRegistrationApi {

    private final CourseRegistrationService courseRegistrationService;

    @GetMapping("/course/registration/precourse")
    public ResponseEntity<List<PreCourseRegistrationLectureResponse>> getPreRegistrationLecture(
        @RequestParam(value = "timetable_frame_id") Integer timetableFrameId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        List<PreCourseRegistrationLectureResponse> response = courseRegistrationService.getUserPreRegistrationCourses(
            timetableFrameId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/registration/search")
    public ResponseEntity<List<CourseRegistrationLectureResponse>> searchLectures(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String semester
    ) {
        List<CourseRegistrationLectureResponse> lectures = courseRegistrationService.searchCourseRegistrationLecture(
            name, department, year, semester);
        return ResponseEntity.ok(lectures);
    }
}
