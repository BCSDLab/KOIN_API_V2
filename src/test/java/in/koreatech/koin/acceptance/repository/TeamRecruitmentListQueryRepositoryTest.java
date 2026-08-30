package in.koreatech.koin.acceptance.repository;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.CONTEST;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.STUDY;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.OFFLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentSort.DEADLINE_ASC;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentSort.LATEST_DESC;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.DELETED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatusFilter.ALL;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.ROLE_BASED;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatusFilter;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentListQueryRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;

/**
 * 모집글 목록 조회 QueryDSL 검증. 필터 조합과 정렬, 페이지 경계, 삭제 제외를 실제 DB 로 확인한다.
 */
class TeamRecruitmentListQueryRepositoryTest extends AcceptanceTest {

    @Autowired
    private TeamRecruitmentListQueryRepository listQueryRepository;

    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    private Student author;
    private Student otherAuthor;

    @BeforeEach
    void setUp() {
        clear();
        Department department = departmentFixture.컴퓨터공학부();
        author = userFixture.준호_학생(department, null);
        otherAuthor = userFixture.성빈_학생(department);
    }

    private TeamRecruitment save(
        Student writer,
        String title,
        TeamRecruitmentCategory category,
        TeamRecruitmentMeetingType meetingType,
        TeamRecruitmentStatus status,
        LocalDate deadline
    ) {
        return recruitmentRepository.save(TeamRecruitment.builder()
            .author(writer.getUser())
            .category(category)
            .title(title)
            .meetingType(meetingType)
            .activityStartDate(LocalDate.now(clock).plusDays(10))
            .activityEndDate(LocalDate.now(clock).plusDays(20))
            .deadlineDate(deadline)
            .recruitmentType(GENERAL)
            .maxParticipants(5)
            .currentParticipants(0)
            .description("모집 내용")
            .status(status)
            .deletedAt(status == DELETED ? LocalDateTime.now(clock) : null)
            .build());
    }

    private TeamRecruitment save(String title, TeamRecruitmentCategory category) {
        return save(author, title, category, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));
    }

    private TeamRecruitment saveWithRoles(String title, String... roleNames) {
        TeamRecruitment recruitment = TeamRecruitment.builder()
            .author(author.getUser())
            .category(PROJECT)
            .title(title)
            .meetingType(ONLINE)
            .activityStartDate(LocalDate.now(clock).plusDays(10))
            .activityEndDate(LocalDate.now(clock).plusDays(20))
            .deadlineDate(LocalDate.now(clock).plusDays(3))
            .recruitmentType(ROLE_BASED)
            .maxParticipants(roleNames.length)
            .currentParticipants(0)
            .description("모집 내용")
            .status(RECRUITING)
            .build();
        for (int index = 0; index < roleNames.length; index++) {
            recruitment.addRole(TeamRecruitmentRole.builder()
                .name(roleNames[index])
                .maxParticipants(1)
                .currentParticipants(0)
                .displayOrder(index + 1)
                .build());
        }
        return recruitmentRepository.save(recruitment);
    }

    private List<String> titlesOf(List<TeamRecruitment> recruitments) {
        return recruitments.stream().map(TeamRecruitment::getTitle).toList();
    }

    private List<TeamRecruitment> findAll(
        String keyword,
        TeamRecruitmentStatusFilter statusFilter,
        List<TeamRecruitmentCategory> categories,
        TeamRecruitmentMeetingType meetingType,
        in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentSort sort,
        int page,
        int limit
    ) {
        long total = listQueryRepository.count(keyword, statusFilter, categories, meetingType);
        Criteria criteria = Criteria.of(page, limit, (int) total);
        return listQueryRepository.findAll(keyword, statusFilter, categories, meetingType, sort, criteria);
    }

    @Nested
    @DisplayName("keyword 검색")
    class KeywordSearch {

        @Test
        @DisplayName("제목에 포함된 검색어로 찾는다")
        void searchesByTitle() {
            save("AI 공모전 팀원 모집", CONTEST);
            save("알고리즘 스터디", STUDY);

            List<TeamRecruitment> found = findAll("공모전", ALL, null, null, LATEST_DESC, 1, 10);

            assertThat(titlesOf(found)).containsExactly("AI 공모전 팀원 모집");
        }

        @Test
        @DisplayName("검색어는 대소문자를 구분하지 않는다")
        void searchesCaseInsensitively() {
            save("Backend Study", STUDY);

            assertThat(findAll("backend", ALL, null, null, LATEST_DESC, 1, 10)).hasSize(1);
        }

        @Test
        @DisplayName("본문은 검색 대상이 아니다")
        void doesNotSearchDescription() {
            save("제목에는 없음", STUDY);

            assertThat(findAll("모집 내용", ALL, null, null, LATEST_DESC, 1, 10)).isEmpty();
        }

        @Test
        @DisplayName("카테고리 표시명으로 찾는다")
        void searchesByCategoryDisplayName() {
            save("이름에 없음", STUDY);
            save("여기도 없음", CONTEST);

            assertThat(titlesOf(findAll("스터디", ALL, null, null, LATEST_DESC, 1, 10)))
                .containsExactly("이름에 없음");
        }

        @Test
        @DisplayName("진행 방식 표시명으로 찾는다")
        void searchesByMeetingTypeDisplayName() {
            save(author, "온라인 글", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));
            save(author, "대면 글", PROJECT, OFFLINE, RECRUITING, LocalDate.now(clock).plusDays(3));

            assertThat(titlesOf(findAll("오프라인", ALL, null, null, LATEST_DESC, 1, 10)))
                .containsExactly("대면 글");
        }

        @Test
        @DisplayName("역할명으로 찾고 모집글이 중복되지 않는다")
        void searchesByRoleName() {
            TeamRecruitment withRoles = saveWithRoles("역할 있는 글", "Backend", "Frontend");
            save("역할 없는 글", STUDY);

            List<TeamRecruitment> found = findAll("backend", ALL, null, null, LATEST_DESC, 1, 10);

            assertThat(titlesOf(found)).containsExactly("역할 있는 글");
            assertThat(found.get(0).getId()).isEqualTo(withRoles.getId());
            assertThat(listQueryRepository.count("backend", ALL, null, null)).isEqualTo(1);
        }

        @Test
        @DisplayName("여러 역할명이 검색어를 포함해도 모집글은 한 번만 나온다")
        void doesNotDuplicateWhenSeveralRolesMatch() {
            saveWithRoles("중복 확인", "PM Back", "Sub Back");

            assertThat(findAll("back", ALL, null, null, LATEST_DESC, 1, 10)).hasSize(1);
            assertThat(listQueryRepository.count("back", ALL, null, null)).isEqualTo(1);
        }

        @Test
        @DisplayName("카테고리 enum 영문 이름은 검색 대상이 아니다")
        void doesNotSearchCategoryEnumName() {
            save("가나다", CONTEST);

            assertThat(findAll("test", ALL, null, null, LATEST_DESC, 1, 10)).isEmpty();
            assertThat(findAll("contest", ALL, null, null, LATEST_DESC, 1, 10)).isEmpty();
            assertThat(listQueryRepository.count("test", ALL, null, null)).isZero();
        }

        @Test
        @DisplayName("진행 방식 enum 영문 이름은 검색 대상이 아니다")
        void doesNotSearchMeetingTypeEnumName() {
            save(author, "가나다", CONTEST, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));

            assertThat(findAll("line", ALL, null, null, LATEST_DESC, 1, 10)).isEmpty();
            assertThat(findAll("online", ALL, null, null, LATEST_DESC, 1, 10)).isEmpty();
        }

        @Test
        @DisplayName("표시명 검색은 여전히 동작한다")
        void stillSearchesDisplayName() {
            save("가나다", CONTEST);

            assertThat(titlesOf(findAll("공모전", ALL, null, null, LATEST_DESC, 1, 10)))
                .containsExactly("가나다");
            assertThat(titlesOf(findAll("온라인", ALL, null, null, LATEST_DESC, 1, 10)))
                .containsExactly("가나다");
        }

        @Test
        @DisplayName("검색어가 공백이면 필터로 쓰지 않는다")
        void blankKeywordIsIgnored() {
            save("첫 번째", STUDY);
            save("두 번째", STUDY);

            assertThat(findAll("   ", ALL, null, null, LATEST_DESC, 1, 10)).hasSize(2);
        }
    }

    @Nested
    @DisplayName("필터 조합")
    class Filters {

        @Test
        @DisplayName("카테고리를 복수로 지정할 수 있다")
        void filtersByCategories() {
            save("공모전", CONTEST);
            save("스터디", STUDY);
            save("프로젝트", PROJECT);

            List<TeamRecruitment> found =
                findAll(null, ALL, List.of(CONTEST, PROJECT), null, LATEST_DESC, 1, 10);

            assertThat(titlesOf(found)).containsExactlyInAnyOrder("공모전", "프로젝트");
        }

        @Test
        @DisplayName("진행 방식으로 걸러낸다")
        void filtersByMeetingType() {
            save(author, "온라인", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));
            save(author, "오프라인", STUDY, OFFLINE, RECRUITING, LocalDate.now(clock).plusDays(3));

            assertThat(titlesOf(findAll(null, ALL, null, OFFLINE, LATEST_DESC, 1, 10)))
                .containsExactly("오프라인");
        }

        @Test
        @DisplayName("모집 상태로 걸러낸다")
        void filtersByStatus() {
            save(author, "모집중", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));
            save(author, "마감", STUDY, ONLINE, CLOSED, LocalDate.now(clock).plusDays(3));

            assertThat(titlesOf(findAll(null, TeamRecruitmentStatusFilter.RECRUITING, null, null,
                LATEST_DESC, 1, 10))).containsExactly("모집중");
            assertThat(titlesOf(findAll(null, TeamRecruitmentStatusFilter.CLOSED, null, null,
                LATEST_DESC, 1, 10))).containsExactly("마감");
            assertThat(findAll(null, ALL, null, null, LATEST_DESC, 1, 10)).hasSize(2);
        }

        @Test
        @DisplayName("검색어와 카테고리, 진행 방식을 함께 적용한다")
        void combinesFilters() {
            save(author, "AI 공모전", CONTEST, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));
            save(author, "AI 공모전 오프라인", CONTEST, OFFLINE, RECRUITING, LocalDate.now(clock).plusDays(3));
            save(author, "AI 스터디", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));

            assertThat(titlesOf(findAll("AI", ALL, List.of(CONTEST), ONLINE, LATEST_DESC, 1, 10)))
                .containsExactly("AI 공모전");
        }

        @Test
        @DisplayName("삭제된 모집글은 어떤 필터에서도 제외된다")
        void excludesDeleted() {
            save(author, "살아있음", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));
            save(author, "삭제됨", STUDY, ONLINE, DELETED, LocalDate.now(clock).plusDays(3));

            assertThat(titlesOf(findAll(null, ALL, null, null, LATEST_DESC, 1, 10)))
                .containsExactly("살아있음");
            assertThat(listQueryRepository.count(null, ALL, null, null)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("정렬")
    class Sorting {

        @Test
        @DisplayName("LATEST_DESC 는 최근에 생성된 모집글이 먼저다")
        void sortsByLatest() {
            save("첫 번째", STUDY);
            save("두 번째", STUDY);
            save("세 번째", STUDY);

            assertThat(titlesOf(findAll(null, ALL, null, null, LATEST_DESC, 1, 10)))
                .containsExactly("세 번째", "두 번째", "첫 번째");
        }

        @Test
        @DisplayName("DEADLINE_ASC 는 마감이 임박한 모집글이 먼저다")
        void sortsByDeadline() {
            save(author, "늦은 마감", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(9));
            save(author, "빠른 마감", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(1));
            save(author, "중간 마감", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(5));

            assertThat(titlesOf(findAll(null, ALL, null, null, DEADLINE_ASC, 1, 10)))
                .containsExactly("빠른 마감", "중간 마감", "늦은 마감");
        }
    }

    @Nested
    @DisplayName("페이지 경계")
    class Paging {

        @BeforeEach
        void saveFive() {
            for (int index = 1; index <= 5; index++) {
                save("모집글" + index, STUDY);
            }
        }

        @Test
        @DisplayName("첫 페이지는 limit 만큼 반환한다")
        void firstPage() {
            assertThat(titlesOf(findAll(null, ALL, null, null, LATEST_DESC, 1, 2)))
                .containsExactly("모집글5", "모집글4");
        }

        @Test
        @DisplayName("마지막 페이지는 남은 개수만 반환한다")
        void lastPage() {
            assertThat(titlesOf(findAll(null, ALL, null, null, LATEST_DESC, 3, 2)))
                .containsExactly("모집글1");
        }

        @Test
        @DisplayName("전체 페이지를 넘는 page 는 마지막 페이지로 보정된다")
        void pageBeyondLastIsClamped() {
            assertThat(titlesOf(findAll(null, ALL, null, null, LATEST_DESC, 99, 2)))
                .containsExactly("모집글1");
        }

        @Test
        @DisplayName("1보다 작은 page 는 첫 페이지로 보정된다")
        void pageBelowOneIsClamped() {
            assertThat(titlesOf(findAll(null, ALL, null, null, LATEST_DESC, 0, 2)))
                .containsExactly("모집글5", "모집글4");
        }

        @Test
        @DisplayName("count 는 필터를 적용한 전체 개수를 반환한다")
        void countAppliesFilter() {
            assertThat(listQueryRepository.count(null, ALL, null, null)).isEqualTo(5);
            assertThat(listQueryRepository.count("모집글1", ALL, null, null)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("작성자별 조회")
    class ByAuthor {

        @Test
        @DisplayName("작성자의 모집글만 반환한다")
        void filtersByAuthor() {
            save(author, "내 글", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));
            save(otherAuthor, "남의 글", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));

            Integer authorId = author.getUser().getId();
            long total = listQueryRepository.countByAuthor(authorId, ALL);
            Criteria criteria = Criteria.of(1, 10, (int) total);

            assertThat(total).isEqualTo(1);
            assertThat(titlesOf(listQueryRepository.findAllByAuthor(authorId, ALL, LATEST_DESC, criteria)))
                .containsExactly("내 글");
        }

        @Test
        @DisplayName("작성자별 조회에서도 삭제된 모집글은 제외된다")
        void excludesDeletedForAuthor() {
            save(author, "살아있음", STUDY, ONLINE, RECRUITING, LocalDate.now(clock).plusDays(3));
            save(author, "삭제됨", STUDY, ONLINE, DELETED, LocalDate.now(clock).plusDays(3));

            assertThat(listQueryRepository.countByAuthor(author.getUser().getId(), ALL)).isEqualTo(1);
        }
    }
}
