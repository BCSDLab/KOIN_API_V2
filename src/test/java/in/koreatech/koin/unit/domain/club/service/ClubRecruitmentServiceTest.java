package in.koreatech.koin.unit.domain.club.service;

import in.koreatech.koin.common.event.ClubRecruitmentChangeEvent;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.recruitment.model.ClubRecruitment;
import in.koreatech.koin.domain.club.recruitment.repository.ClubRecruitmentRepository;
import in.koreatech.koin.domain.club.recruitment.repository.ClubRecruitmentSubscriptionRepository;
import in.koreatech.koin.domain.club.recruitment.service.ClubRecruitmentService;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
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
    private UserRepository userRepository;

    @Mock
    private ClubRecruitmentSubscriptionRepository clubRecruitmentSubscriptionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
        void 동아리_모집이_이미_등록되어_있으면_예외를_발생한다() {
            // given
            when(clubRecruitmentRepository.findByClub(club)).thenReturn(Optional.of(ClubRecruitment.builder().build()));

            // when / then
            assertThatThrownBy(() -> clubRecruitmentService.createRecruitment(request, clubId, studentId))
                .isInstanceOf(CustomException.class)
                .hasMessage("동아리 공고가 이미 존재합니다.");
        }
    }
}
