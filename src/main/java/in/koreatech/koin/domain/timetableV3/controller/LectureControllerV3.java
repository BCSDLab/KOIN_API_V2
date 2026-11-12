package in.koreatech.koin.domain.timetableV3.controller;

import java.util.List;

import in.koreatech.koin.domain.language.model.SupportedLanguage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetableV3.dto.response.LectureResponseV3;
import in.koreatech.koin.domain.timetableV3.service.LectureServiceV3;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LectureControllerV3 implements LectureApiV3 {

    private final LectureServiceV3 lectureServiceV3;

    @GetMapping("/v3/lectures")
    public ResponseEntity<List<LectureResponseV3>> getLectures(
        @RequestParam(name = "year") Integer year,
        @RequestParam(name = "term") String term,
        @RequestParam(defaultValue = "ko-KR") String language
    ) {
        SupportedLanguage selectedLanguage = SupportedLanguage.fromCode(language);
        List<LectureResponseV3> response = lectureServiceV3.getLectures(year, term).stream()
            .map(lecture -> lecture.withLanguage(selectedLanguage)).toList();
        return ResponseEntity.ok(response);
    }
}
