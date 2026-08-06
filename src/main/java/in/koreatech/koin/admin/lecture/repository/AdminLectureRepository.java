package in.koreatech.koin.admin.lecture.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.Lecture;

public interface AdminLectureRepository extends Repository<Lecture, Integer> {

    boolean existsBySemesterAndCodeAndLectureClass(String semester, String code, String lectureClass);

    List<Lecture> saveAll(Iterable<Lecture> lectures);
}
