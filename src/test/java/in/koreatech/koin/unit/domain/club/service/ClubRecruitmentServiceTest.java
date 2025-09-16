package in.koreatech.koin.unit.domain.club.service;

import in.koreatech.koin.common.event.ClubRecruitmentChangeEvent;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.manager.service.ClubManagerService;
import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentModifyRequest;
import in.koreatech.koin.domain.club.recruitment.model.ClubRecruitment;
import in.koreatech.koin.domain.club.recruitment.repository.ClubRecruitmentRepository;
import in.koreatech.koin.domain.club.recruitment.service.ClubRecruitmentService;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.ClubFixture;
import in.koreatech.koin.unit.fixture.StudentFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClubRecruitmentServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClubRecruitmentRepository clubRecruitmentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ClubManagerService clubManagerService;

    @InjectMocks
    private ClubRecruitmentService clubRecruitmentService;

    @Nested
    class CreateRecruitment {

        Integer clubId;
        Integer studentId;
        Club club;
        Student student;
        boolean isAlwaysRecruiting;
        String imageUrl;
        String content;
        ClubRecruitmentCreateRequest request;

        @BeforeEach
        void init() {
            clubId = 1;
            studentId = 1;
            club = ClubFixture.활성화_BCSD_동아리(clubId);
            student = StudentFixture.익명_학생(mock(Department.class));
            isAlwaysRecruiting = false;
            imageUrl = "https://bcsdlab.com/static/img/logo.d89d9cc.png";
            content = "BCSD LAB 모집";

            request = new ClubRecruitmentCreateRequest(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                isAlwaysRecruiting,
                imageUrl,
                content
            );

            ReflectionTestUtils.setField(student, "id", studentId);

            when(clubRepository.getById(clubId)).thenReturn(club);
            when(studentRepository.getById(studentId)).thenReturn(student);
        }

        @Test
        void 동아리_모집을_생성한다() {
            // given
            when(clubRecruitmentRepository.findByClub(club)).thenReturn(Optional.empty());

            // when
            clubRecruitmentService.createRecruitment(request, clubId, studentId);

            // then
            ArgumentCaptor<ClubRecruitment> recruitmentCaptor = ArgumentCaptor.forClass(ClubRecruitment.class);
            ArgumentCaptor<ClubRecruitmentChangeEvent> eventCaptor = ArgumentCaptor.forClass(ClubRecruitmentChangeEvent.class);

            verify(clubRecruitmentRepository).save(recruitmentCaptor.capture());
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            ClubRecruitment clubRecruitment = recruitmentCaptor.getValue();
            ClubRecruitmentChangeEvent event = eventCaptor.getValue();

            assertThat(clubRecruitment.getStartDate()).isEqualTo(LocalDate.now());
            assertThat(clubRecruitment.getEndDate()).isEqualTo(LocalDate.now().plusDays(1));
            assertThat(clubRecruitment.getIsAlwaysRecruiting()).isEqualTo(isAlwaysRecruiting);
            assertThat(clubRecruitment.getImageUrl()).isEqualTo(imageUrl);
            assertThat(clubRecruitment.getContent()).isEqualTo(content);

            assertThat(event.clubName()).isEqualTo(club.getName());
            assertThat(event.clubId()).isEqualTo(club.getId());
        }

        @Test
        void 관리자가_아닌_학생이_모집을_생성하면_예외를_발생한다() {
            // given
            doThrow(AuthorizationException.withDetail("studentId: " + studentId))
                .when(clubManagerService)
                .isClubManager(clubId, studentId);

            // when / then
            assertThatThrownBy(() -> clubRecruitmentService.createRecruitment(request, clubId, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }

        @Test
        void 동아리_모집이_이미_등록되어_있으면_예외를_발생한다() {
            // given
            when(clubRecruitmentRepository.findByClub(club)).thenReturn(Optional.of(ClubRecruitment.builder().build()));

            // when / then
            assertThatThrownBy(() -> clubRecruitmentService.createRecruitment(request, clubId, studentId))
                .isInstanceOf(CustomException.class)
                .hasMessage("동아리 공고가 이미 존재합니다.");
        }
    }

    @Nested
    class ModifyRecruitment {

        ClubRecruitmentModifyRequest request;
        Integer clubRecruitmentId;
        Integer clubId;
        Integer studentId;
        Club club;
        Student student;
        ClubRecruitment clubRecruitment;

        @BeforeEach
        void init() {
            request = new ClubRecruitmentModifyRequest(
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                false,
                "https://bcsdlab.com/static/img/new-logo.png",
                "수정된 내용"
            );

            clubRecruitmentId = 1;
            clubId = 1;
            studentId = 1;

            club = ClubFixture.활성화_BCSD_동아리(clubId);
            student = StudentFixture.익명_학생(mock(Department.class));
            clubRecruitment = 모집_공고(clubRecruitmentId, club);

            when(clubRepository.getById(clubId)).thenReturn(club);
            when(clubRecruitmentRepository.getByClub(club)).thenReturn(clubRecruitment);
            when(studentRepository.getById(studentId)).thenReturn(student);
        }

        @Test
        void 동아리_모집을_수정한다() {
            // when
            clubRecruitmentService.modifyRecruitment(request, clubId, studentId);

            // then
            assertThat(clubRecruitment.getStartDate()).isEqualTo(request.startDate());
            assertThat(clubRecruitment.getEndDate()).isEqualTo(request.endDate());
            assertThat(clubRecruitment.getIsAlwaysRecruiting()).isEqualTo(request.isAlwaysRecruiting());
            assertThat(clubRecruitment.getImageUrl()).isEqualTo(request.imageUrl());
            assertThat(clubRecruitment.getContent()).isEqualTo(request.content());
            assertThat(clubRecruitment.getClub()).isEqualTo(club);

            ArgumentCaptor<ClubRecruitmentChangeEvent> eventCaptor = ArgumentCaptor.forClass(ClubRecruitmentChangeEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            ClubRecruitmentChangeEvent event = eventCaptor.getValue();

            assertThat(event.clubId()).isEqualTo(club.getId());
            assertThat(event.clubName()).isEqualTo(club.getName());
        }

        @Test
        void 관리자가_아닌_학생이_동아리_모집을_수정하면_예외를_발생한다() {
            doThrow(AuthorizationException.withDetail("studentId: " + studentId))
                .when(clubManagerService)
                .isClubManager(club.getId(), student.getId());

            // when / then
            assertThatThrownBy(() -> clubRecruitmentService.modifyRecruitment(request, clubId, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }
    }

    private ClubRecruitment 모집_공고(Integer id, Club club) {
        return ClubRecruitment
            .builder()
            .id(id)
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2025, 9, 15))
            .isAlwaysRecruiting(false)
            .imageUrl("https://bcsdlab.com/static/img/logo.d89d9cc.png")
            .content("모집 내용")
            .club(club)
            .build();
    }
}
