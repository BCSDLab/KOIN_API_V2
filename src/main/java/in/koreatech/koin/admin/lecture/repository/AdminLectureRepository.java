package in.koreatech.koin.admin.lecture.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.Lecture;

public interface AdminLectureRepository extends Repository<Lecture, Integer> {

    boolean existsBySemesterAndCodeAndLectureClass(String semester, String code, String lectureClass);

    Lecture save(Lecture lecture);
}
