package in.koreatech.koin.domain.timetableV3.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV3.dto.response.SemesterCheckResponseV3;
import in.koreatech.koin.domain.timetableV3.dto.response.SemesterResponseV3;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableFrameRepositoryV3;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SemesterServiceV3 {

    private final TimetableFrameRepositoryV3 timetableFrameRepository;
    private final SemesterRepositoryV3 semesterRepository;

    public List<SemesterResponseV3> getSemesters() {
        return semesterRepository.findAll().stream()
            .sorted(Comparator.comparing(Semester::getYear).reversed()
                .thenComparing(semester -> semester.getTerm().getPriority()))
            .map(SemesterResponseV3::of)
            .toList();
    }

    public SemesterCheckResponseV3 getStudentSemesters(Integer userId) {
        List<Semester> semesters = timetableFrameRepository.findByUserIdAndIsMainTrue(userId).stream()
            .map(TimetableFrame::getSemester)
            .distinct()
            .sorted(Comparator.comparing(Semester::getYear).reversed()
                .thenComparing(semester -> semester.getTerm().getPriority()))
            .toList();
        return SemesterCheckResponseV3.of(semesters);
    }
}
