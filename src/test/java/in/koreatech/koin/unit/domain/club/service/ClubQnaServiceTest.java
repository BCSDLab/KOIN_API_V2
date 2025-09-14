package in.koreatech.koin.unit.domain.club.service;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.manager.repository.ClubManagerRepository;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.qna.dto.request.ClubQnaCreateRequest;
import in.koreatech.koin.domain.club.qna.dto.response.ClubQnasResponse;
import in.koreatech.koin.domain.club.qna.model.ClubQna;
import in.koreatech.koin.domain.club.qna.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.qna.service.ClubQnaService;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.unit.fixture.ClubFixture;
import in.koreatech.koin.unit.fixture.ClubQnaFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ClubQnaServiceTest {

    @Mock
    private ClubQnaRepository clubQnaRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClubManagerRepository clubManagerRepository;

    @InjectMocks
    private ClubQnaService clubQnaService;

    @Nested
    class GetQnas {

        Integer clubId;
        Integer qnaId1;
        Integer qnaId2;

        String content1;
        String content2;

        ClubQna qna1;
        ClubQna qna2;

        List<ClubQna> qnas;

        @BeforeEach
        void init() {
            clubId = 1;
            qnaId1 = 1;
            qnaId2 = 2;

            content1 = "질문 내용 1";
            content2 = "질문 내용 2";

            qna1 = ClubQnaFixture.QNA(clubId, qnaId1, content1);
            qna2 = ClubQnaFixture.QNA(clubId, qnaId2, content2);

            ReflectionTestUtils.setField(qna1, "createdAt", LocalDateTime.now().minusMinutes(1));

            qnas = List.of(qna1, qna2);
        }

        @Test
        void 동아리의_QnA들을_조회하면_최신순으로_QnA_리스트가_반환된다() {
            // given
            when(clubQnaRepository.findAllByClubId(clubId)).thenReturn(qnas);

            // when
            ClubQnasResponse response = clubQnaService.getQnas(clubId);

            // then
            ClubQnasResponse.InnerQnaResponse earlierQna = response.qnas().get(0);
            ClubQnasResponse.InnerQnaResponse laterQna = response.qnas().get(1);

            verify(clubQnaRepository).findAllByClubId(clubId);
            assertThat(response.qnas()).hasSize(2);

            assertThat(earlierQna.id()).isEqualTo(qnaId2);
            assertThat(earlierQna.content()).isEqualTo(content2);

            assertThat(laterQna.id()).isEqualTo(qnaId1);
            assertThat(laterQna.content()).isEqualTo(content1);

            assertThat(earlierQna.createdAt()).isAfter(laterQna.createdAt());
        }
    }

    @Nested
    class CreateQna {

        Integer clubId;
        Integer studentId;
        Club club;
        Student student;
        ClubQna parentQna;
        ClubQnaCreateRequest question;
        ClubQnaCreateRequest answer;

        @BeforeEach
        void init() {
            clubId = 1;
            studentId = 1;
            club = ClubFixture.활성화_BCSD_동아리(clubId);
            student = mock(Student.class);
            parentQna = ClubQnaFixture.QNA(clubId, 1, "질문");
            question = new ClubQnaCreateRequest(null, "질문");
            answer = new ClubQnaCreateRequest(1, "답변");

            when(clubRepository.getById(clubId)).thenReturn(club);
            when(studentRepository.getById(studentId)).thenReturn(student);
        }

        @Test
        void 관리자가_아닌_학생이_질문을_작성한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(false);

            // when
            clubQnaService.createQna(question, clubId, studentId);

            // then
            ArgumentCaptor<ClubQna> qnaCaptor = ArgumentCaptor.forClass(ClubQna.class);

            verify(clubQnaRepository).save(qnaCaptor.capture());

            ClubQna clubQna = qnaCaptor.getValue();

            assertThat(clubQna.getClub()).isEqualTo(club);
            assertThat(clubQna.getAuthor()).isEqualTo(student);
            assertThat(clubQna.getParent()).isNull();
            assertThat(clubQna.getContent()).isEqualTo(question.content());
            assertThat(clubQna.getIsManager()).isFalse();
            assertThat(clubQna.getIsDeleted()).isFalse();
        }

        @Test
        void 관리자인_학생이_답변을_작성한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubQnaRepository.getById(answer.parentId())).thenReturn(parentQna);

            // when
            clubQnaService.createQna(answer, clubId, studentId);

            // then
            ArgumentCaptor<ClubQna> qnaCaptor = ArgumentCaptor.forClass(ClubQna.class);

            verify(clubQnaRepository).save(qnaCaptor.capture());

            ClubQna clubQna = qnaCaptor.getValue();

            assertThat(clubQna.getClub()).isEqualTo(club);
            assertThat(clubQna.getAuthor()).isEqualTo(student);
            assertThat(clubQna.getParent()).isEqualTo(parentQna);
            assertThat(clubQna.getContent()).isEqualTo(answer.content());
            assertThat(clubQna.getIsManager()).isTrue();
            assertThat(clubQna.getIsDeleted()).isFalse();
        }

        @Test
        void 관리자가_아닌_학생이_답변을_작성하면_예외를_발생한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubQnaService.createQna(answer, clubId, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }

        @Test
        void 관리자인_학생이_질문을_작성하면_예외를_발생한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> clubQnaService.createQna(question, clubId, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }
    }

    @Nested
    class DeleteQna {

        Integer clubId;
        Integer qnaId;
        Integer studentId;
        Student student;
        ClubQna clubQna;

        @BeforeEach
        void init() {
            clubId = 1;
            qnaId = 1;
            studentId = 1;
            student = mock(Student.class);
            clubQna = spy(ClubQnaFixture.QNA(clubId, qnaId, "질문"));

            when(clubQnaRepository.getById(qnaId)).thenReturn(clubQna);
            when(clubQna.getAuthor()).thenReturn(student);
            when(student.getId()).thenReturn(studentId);
        }

        @Test
        void 관리자가_아닌_학생이_자신이_작성한_질문을_삭제한다() {
            // when
            clubQnaService.deleteQna(clubId, qnaId, studentId);

            // then
            verify(clubQna).detachFromParentIfChild();
            verify(clubQnaRepository).delete(clubQna);
        }

        @Test
        void 관리자인_학생이_자신이_작성하지_않은_QNA_글을_삭제한다() {
            // given
            Integer managerId = 2;

            when(clubManagerRepository.existsByClubIdAndUserId(clubId, managerId)).thenReturn(true);

            // when
            clubQnaService.deleteQna(clubId, qnaId, managerId);

            // then
            verify(clubQna).detachFromParentIfChild();
            verify(clubQnaRepository).delete(clubQna);
        }

        @Test
        void 관리자가_아닌_학생이_자신이_작성하지_않은_QNA_글을_삭제하면_예외를_발생한다() {
            // given
            Integer requesterId = 2;

            when(clubManagerRepository.existsByClubIdAndUserId(clubId, requesterId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubQnaService.deleteQna(clubId, qnaId, requesterId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");

            verify(clubQna, never()).detachFromParentIfChild();
            verify(clubQnaRepository, never()).delete(clubQna);
        }
    }
}
