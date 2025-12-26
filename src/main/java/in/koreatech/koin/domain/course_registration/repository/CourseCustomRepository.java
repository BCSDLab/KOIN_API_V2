package in.koreatech.koin.domain.course_registration.repository;

import static in.koreatech.koin.domain.timetable.model.QLecture.lecture;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.course_registration.dto.LectureSearchCriteria;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CourseCustomRepository {

    private final JPAQueryFactory queryFactory;

    public List<Lecture> searchLectures(LectureSearchCriteria condition) {
        return queryFactory
            .selectFrom(lecture)
            .where(
                nameContains(condition.name()),
                departmentEq(condition.department()),
                semesterDateEq(condition.getSemesterDate())
            )
            .orderBy(lecture.semester.desc(), lecture.code.asc())
            .fetch();
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? lecture.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression departmentEq(String department) {
        return department != null ? lecture.department.eq(department) : null;
    }

    private BooleanExpression semesterDateEq(String semesterDate) {
        return semesterDate != null ? lecture.semester.eq(semesterDate) : null;
    }
}
