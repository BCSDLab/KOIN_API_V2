package in.koreatech.koin.unit.domain.club.service;

import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.club.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.event.dto.request.ClubEventCreateRequest;
import in.koreatech.koin.domain.club.event.dto.request.ClubEventModifyRequest;
import in.koreatech.koin.domain.club.event.dto.response.ClubEventResponse;
import in.koreatech.koin.domain.club.event.dto.response.ClubEventsResponse;
import in.koreatech.koin.domain.club.event.enums.ClubEventStatus;
import in.koreatech.koin.domain.club.event.enums.ClubEventType;
import in.koreatech.koin.domain.club.event.model.ClubEvent;
import in.koreatech.koin.domain.club.event.model.ClubEventImage;
import in.koreatech.koin.domain.club.event.repository.ClubEventImageRepository;
import in.koreatech.koin.domain.club.event.repository.ClubEventRepository;
import in.koreatech.koin.domain.club.event.repository.ClubEventSubscriptionRepository;
import in.koreatech.koin.domain.club.event.service.ClubEventService;
import in.koreatech.koin.domain.club.manager.service.ClubManagerService;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClubEventServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClubEventRepository clubEventRepository;

    @Mock
    private ClubEventImageRepository clubEventImageRepository;

    @Mock
    private ClubEventSubscriptionRepository clubEventSubscriptionRepository;

    @Mock
    private ClubManagerService clubManagerService;

    @InjectMocks
    private ClubEventService clubEventService;

    @Nested
    class CreateClubEvent {

        ClubEventCreateRequest request;
        Integer clubId;
        Integer studentId;
        Club club;
        Student student;

        @BeforeEach
        void init() {
            clubId = 1;
            studentId = 1;

            club = ClubFixture.활성화_BCSD_동아리(clubId);
            student = StudentFixture.익명_학생(mock(Department.class));

            when(clubRepository.getById(clubId)).thenReturn(club);
            when(studentRepository.getById(studentId)).thenReturn(student);
        }

        @Test
        void 이미지_없이_동아리_행사를_생성한다() {
            // given
            request = new ClubEventCreateRequest(
                "B-CON",
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                "BCSDLab의 멘토 혹은 레귤러들의 경험을 공유해요.",
                "여러 동아리원들과 자신의 생각, 경험에 대해 나눠요."
            );

            // when
            clubEventService.createClubEvent(request, clubId, studentId);

            // then
            ArgumentCaptor<ClubEvent> eventCaptor = ArgumentCaptor.forClass(ClubEvent.class);
            verify(clubEventRepository).save(eventCaptor.capture());
            ClubEvent clubEvent = eventCaptor.getValue();

            assertThat(clubEvent.getClub()).isEqualTo(club);
            assertThat(clubEvent.getName()).isEqualTo(request.name());
            assertThat(clubEvent.getStartDate()).isEqualTo(request.startDate());
            assertThat(clubEvent.getEndDate()).isEqualTo(request.endDate());
            assertThat(clubEvent.getIntroduce()).isEqualTo(request.introduce());
            assertThat(clubEvent.getContent()).isEqualTo(request.content());
            assertThat(clubEvent.getNotifiedBeforeOneHour()).isFalse();

            verify(clubEventImageRepository, never()).save(any(ClubEventImage.class));
        }

        @Test
        void 이미지를_포함해_동아리_행사를_생성한다() {
            // given
            request = new ClubEventCreateRequest(
                "B-CON",
                List.of(
                    "https://bcsdlab.com/static/img/event1.png",
                    "https://bcsdlab.com/static/img/event2.png"
                ),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                "BCSDLab의 멘토 혹은 레귤러들의 경험을 공유해요.",
                "여러 동아리원들과 자신의 생각, 경험에 대해 나눠요."
            );

            // when
            clubEventService.createClubEvent(request, clubId, studentId);

            // then
            ArgumentCaptor<ClubEvent> eventCaptor = ArgumentCaptor.forClass(ClubEvent.class);
            verify(clubEventRepository).save(eventCaptor.capture());
            ClubEvent clubEvent = eventCaptor.getValue();

            assertThat(clubEvent.getClub()).isEqualTo(club);
            assertThat(clubEvent.getName()).isEqualTo(request.name());
            assertThat(clubEvent.getStartDate()).isEqualTo(request.startDate());
            assertThat(clubEvent.getEndDate()).isEqualTo(request.endDate());
            assertThat(clubEvent.getIntroduce()).isEqualTo(request.introduce());
            assertThat(clubEvent.getContent()).isEqualTo(request.content());
            assertThat(clubEvent.getNotifiedBeforeOneHour()).isFalse();

            ArgumentCaptor<ClubEventImage> imageCaptor = ArgumentCaptor.forClass(ClubEventImage.class);
            verify(clubEventImageRepository, times(2)).save(imageCaptor.capture());
            List<ClubEventImage> savedImages = imageCaptor.getAllValues();

            assertThat(savedImages).hasSize(request.imageUrls().size());
            assertThat(savedImages)
                .extracting(ClubEventImage::getImageUrl)
                .containsExactlyInAnyOrderElementsOf(request.imageUrls());
            assertThat(savedImages)
                .allMatch(img -> img.getClubEvent().equals(clubEvent));
        }

        @Test
        void 관리자가_아닌_학생이_동아리_행사를_생성하면_예외를_발생한다() {
            // given
            ReflectionTestUtils.setField(student, "id", studentId);

            doThrow(AuthorizationException.withDetail("studentId: " + studentId))
                .when(clubManagerService)
                .isClubManager(clubId, studentId);

            // when / then
            assertThatThrownBy(() -> clubEventService.createClubEvent(request, clubId, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }
    }

    @Nested
    class ModifyClubEvent {

        ClubEventModifyRequest request;
        Integer eventId;
        Integer clubId;
        Integer studentId;
        ClubEvent clubEvent;
        Club club;
        Student student;

        @BeforeEach
        void init() {
            eventId = 1;
            clubId = 1;
            studentId = 1;

            club = ClubFixture.활성화_BCSD_동아리(clubId);
            clubEvent = 상태별_동아리_행사(eventId, club, ClubEventStatus.UPCOMING, LocalDateTime.now());
            student = StudentFixture.익명_학생(mock(Department.class));

            when(clubRepository.getById(clubId)).thenReturn(club);
            when(clubEventRepository.getById(eventId)).thenReturn(clubEvent);
            when(studentRepository.getById(studentId)).thenReturn(student);
        }

        @Test
        void 이미지_없이_동아리_행사_정보를_수정한다() {
            //given
            request = new ClubEventModifyRequest(
                "수정된 제목",
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                "수정된 행사 내용",
                "수정된 행사 상세 내용"
            );

            // when
            clubEventService.modifyClubEvent(request, eventId, clubId, studentId);

            // then
            assertThat(clubEvent.getName()).isEqualTo(request.name());
            assertThat(clubEvent.getStartDate()).isEqualTo(request.startDate());
            assertThat(clubEvent.getEndDate()).isEqualTo(request.endDate());
            assertThat(clubEvent.getIntroduce()).isEqualTo(request.introduce());
            assertThat(clubEvent.getContent()).isEqualTo(request.content());

            verify(clubEventImageRepository).deleteAllByClubEvent(clubEvent);
            verify(clubEventImageRepository, never()).save(any(ClubEventImage.class));
        }

        @Test
        void 이미지를_포함해_동아리_행사_정보를_수정한다() {
            // given
            request = new ClubEventModifyRequest(
                "수정된 제목",
                List.of(
                    "https://bcsdlab.com/static/img/event1.png",
                    "https://bcsdlab.com/static/img/event2.png"
                ),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                "수정된 행사 내용",
                "수정된 행사 상세 내용"
            );

            // when
            clubEventService.modifyClubEvent(request, eventId, clubId, studentId);

            // then
            assertThat(clubEvent.getName()).isEqualTo(request.name());
            assertThat(clubEvent.getStartDate()).isEqualTo(request.startDate());
            assertThat(clubEvent.getEndDate()).isEqualTo(request.endDate());
            assertThat(clubEvent.getIntroduce()).isEqualTo(request.introduce());
            assertThat(clubEvent.getContent()).isEqualTo(request.content());

            verify(clubEventImageRepository).deleteAllByClubEvent(clubEvent);

            ArgumentCaptor<ClubEventImage> imageCaptor = ArgumentCaptor.forClass(ClubEventImage.class);
            verify(clubEventImageRepository, times(request.imageUrls().size())).save(imageCaptor.capture());
            List<ClubEventImage> savedImages = imageCaptor.getAllValues();

            assertThat(savedImages).hasSize(request.imageUrls().size());
            assertThat(savedImages)
                .extracting(ClubEventImage::getImageUrl)
                .containsExactlyInAnyOrderElementsOf(request.imageUrls());
            assertThat(savedImages)
                .allMatch(img -> img.getClubEvent().equals(clubEvent));
        }

        @Test
        void 관리자가_아닌_학생이_동아리_행사_정보를_수정하면_예외를_발생한다() {
            // given
            ReflectionTestUtils.setField(student, "id", studentId);

            doThrow(AuthorizationException.withDetail("studentId: " + studentId))
                .when(clubManagerService)
                .isClubManager(clubId, studentId);

            // when / then
            assertThatThrownBy(() -> clubEventService.modifyClubEvent(request, eventId, clubId, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }
    }

    @Nested
    class DeleteClubEvent {

        Integer clubId;
        Integer eventId;
        Integer studentId;
        Club club;
        ClubEvent clubEvent;
        Student student;

        @BeforeEach
        void init() {
            clubId = 1;
            eventId = 1;
            studentId = 1;
            club = ClubFixture.활성화_BCSD_동아리(clubId);
            clubEvent = 상태별_동아리_행사(eventId, club, ClubEventStatus.UPCOMING, LocalDateTime.now());
            student = StudentFixture.익명_학생(mock(Department.class));

            when(clubRepository.getById(clubId)).thenReturn(club);
            when(clubEventRepository.getById(eventId)).thenReturn(clubEvent);
            when(studentRepository.getById(studentId)).thenReturn(student);
        }

        @Test
        void 동아리_행사를_삭제하면_동아리_행사_이미지도_삭제된다() {
            // when
            clubEventService.deleteClubEvent(clubId, eventId, studentId);

            // then
            verify(clubEventRepository).delete(clubEvent);
        }

        @Test
        void 관리자가_아닌_학생이_동아리_행사를_삭제하면_예외를_발생한다() {
            // given
            ReflectionTestUtils.setField(student, "id", studentId);

            doThrow(AuthorizationException.withDetail("studentId: " + studentId))
                .when(clubManagerService)
                .isClubManager(clubId, studentId);

            // when / then
            assertThatThrownBy(() -> clubEventService.deleteClubEvent(clubId, eventId, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }
    }

    @Nested
    class GetClubEvent {

        Integer clubId;
        Integer eventId;
        Club club;
        ClubEvent clubEvent;

        @BeforeEach
        void init() {
            clubId = 1;
            eventId = 1;
            club = ClubFixture.활성화_BCSD_동아리(clubId);
            clubEvent = 상태별_동아리_행사(eventId, club, ClubEventStatus.ENDED, LocalDateTime.now());

            when(clubEventRepository.getClubEventByIdAndClubId(eventId, clubId)).thenReturn(clubEvent);
        }

        @Test
        void 특정_동아리_행사를_조회한다() {
            // when
            ClubEventResponse response = clubEventService.getClubEvent(clubId, eventId);

            // then
            assertThat(response.id()).isEqualTo(clubEvent.getId());
            assertThat(response.name()).isEqualTo(clubEvent.getName());
            assertThat(response.imageUrls())
                .containsExactlyElementsOf(
                    clubEvent.getImages().stream().map(ClubEventImage::getImageUrl).toList()
                );
            assertThat(response.startDate()).isEqualTo(clubEvent.getStartDate());
            assertThat(response.endDate()).isEqualTo(clubEvent.getEndDate());
            assertThat(response.introduce()).isEqualTo(clubEvent.getIntroduce());
            assertThat(response.content()).isEqualTo(clubEvent.getContent());
            assertThat(response.status()).isEqualTo(ClubEventStatus.ENDED.name());
        }
    }

    @Nested
    class GetClubEvents {

        Integer clubId;
        ClubEventType clubEventType;
        Integer userId;
        Club club;
        List<ClubEvent> events;

        @BeforeEach
        void init() {
            clubId = 1;
            club = ClubFixture.활성화_BCSD_동아리(clubId);
            events = List.of(
                상태별_동아리_행사(1, club, ClubEventStatus.SOON, LocalDateTime.now().plusSeconds(1)),
                상태별_동아리_행사(2, club, ClubEventStatus.UPCOMING, LocalDateTime.now().plusSeconds(2)),
                상태별_동아리_행사(3, club, ClubEventStatus.ENDED, LocalDateTime.now().plusSeconds(3)),
                상태별_동아리_행사(4, club, ClubEventStatus.ONGOING, LocalDateTime.now().plusSeconds(4))
            );

            when(clubEventRepository.getAllByClubId(clubId)).thenReturn(events);
        }

        @Nested
        class UserIdIsNull {

            @BeforeEach
            void init() {
                userId = null;
            }

            @Test
            void RECENT로_조회하면_종료된_행사는_아래에_정렬되고_그_외는_최신순으로_반환된다() {
                // given
                clubEventType = ClubEventType.RECENT;

                // when
                List<ClubEventsResponse> response = clubEventService.getClubEvents(clubId, clubEventType, userId);

                // then
                assertThat(response).hasSize(events.size())
                    .extracting(
                        ClubEventsResponse::id,
                        ClubEventsResponse::status
                    )
                    .containsExactly(
                        tuple(4, ClubEventStatus.ONGOING.name()),
                        tuple(2, ClubEventStatus.UPCOMING.name()),
                        tuple(1, ClubEventStatus.SOON.name()),
                        tuple(3, ClubEventStatus.ENDED.name())
                    );
            }

            @Test
            void UPCOMING으로_조회하면_시작_1시간_이상_남은_행사만_반환된다() {
                // given
                clubEventType = ClubEventType.UPCOMING;

                // when
                List<ClubEventsResponse> response = clubEventService.getClubEvents(clubId, clubEventType, userId);

                // then
                assertThat(response)
                    .extracting(
                        ClubEventsResponse::id,
                        ClubEventsResponse::status
                    )
                    .containsExactly(
                        tuple(2, ClubEventStatus.UPCOMING.name())
                    );
            }

            @Test
            void ONGOING으로_조회하면_시작_1시간_전과_진행중인_행사만_반환된다() {
                // given
                clubEventType = ClubEventType.ONGOING;

                // when
                List<ClubEventsResponse> response = clubEventService.getClubEvents(clubId, clubEventType, userId);

                // then
                assertThat(response)
                    .extracting(
                        ClubEventsResponse::id,
                        ClubEventsResponse::status
                    )
                    .containsExactly(
                        tuple(4, ClubEventStatus.ONGOING.name()),
                        tuple(1, ClubEventStatus.SOON.name())
                    );
            }

            @Test
            void ENDED로_조회하면_종료된_행사만_반환된다() {
                // given
                clubEventType = ClubEventType.ENDED;

                // when
                List<ClubEventsResponse> response = clubEventService.getClubEvents(clubId, clubEventType, userId);

                // then
                assertThat(response)
                    .extracting(
                        ClubEventsResponse::id,
                        ClubEventsResponse::status
                    )
                    .containsExactly(
                        tuple(3, ClubEventStatus.ENDED.name())
                    );
            }
        }

        @Nested
        class UserIdIsNotNull {

            List<Integer> eventIds;

            @BeforeEach
            void init() {
                userId = 1;
                eventIds = List.of(1, 2, 3, 4);

                when(clubEventSubscriptionRepository.findSubscribedEventIds(userId, eventIds))
                    .thenReturn(List.of(1, 3));
            }

            @Test
            void RECENT로_조회하면_종료된_행사는_아래에_정렬되고_그_외는_최신순으로_반환된다() {
                // given
                clubEventType = ClubEventType.RECENT;

                // when
                List<ClubEventsResponse> response = clubEventService.getClubEvents(clubId, clubEventType, userId);

                // then
                assertThat(response)
                    .hasSize(events.size())
                    .extracting(
                        ClubEventsResponse::id,
                        ClubEventsResponse::status,
                        ClubEventsResponse::isSubscribed
                    )
                    .containsExactly(
                        tuple(4, ClubEventStatus.ONGOING.name(), false),
                        tuple(2, ClubEventStatus.UPCOMING.name(), false),
                        tuple(1, ClubEventStatus.SOON.name(), true),
                        tuple(3, ClubEventStatus.ENDED.name(), true)
                    );
            }

            @Test
            void UPCOMING으로_조회하면_시작_1시간_이상_남은_행사만_반환된다() {
                // given
                clubEventType = ClubEventType.UPCOMING;

                // when
                List<ClubEventsResponse> response = clubEventService.getClubEvents(clubId, clubEventType, userId);

                // then
                assertThat(response)
                    .extracting(ClubEventsResponse::status, ClubEventsResponse::isSubscribed)
                    .containsExactly(
                        tuple(ClubEventStatus.UPCOMING.name(), false)
                    );
            }

            @Test
            void ONGOING으로_조회하면_시작_1시간_전과_진행중인_행사만_반환된다() {
                // given
                clubEventType = ClubEventType.ONGOING;

                // when
                List<ClubEventsResponse> response = clubEventService.getClubEvents(clubId, clubEventType, userId);

                // then
                assertThat(response)
                    .extracting(ClubEventsResponse::status, ClubEventsResponse::isSubscribed)
                    .containsExactly(
                        tuple(ClubEventStatus.ONGOING.name(), false),
                        tuple(ClubEventStatus.SOON.name(), true)
                    );
            }

            @Test
            void ENDED로_조회하면_종료된_행사만_반환된다() {
                // given
                clubEventType = ClubEventType.ENDED;

                // when
                List<ClubEventsResponse> response = clubEventService.getClubEvents(clubId, clubEventType, userId);

                // then
                assertThat(response)
                    .extracting(ClubEventsResponse::status, ClubEventsResponse::isSubscribed)
                    .containsExactly(
                        tuple(ClubEventStatus.ENDED.name(), true)
                    );
            }
        }
    }

    private ClubEvent 상태별_동아리_행사(Integer eventId, Club club, ClubEventStatus clubEventStatus, LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startDate;
        LocalDateTime endDate;

        switch (clubEventStatus) {
            case SOON -> {
                startDate = now.plusHours(1);
                endDate = now.plusDays(1);
            }
            case ONGOING -> {
                startDate = now.minusDays(1);
                endDate = now.plusDays(1);
            }
            case UPCOMING -> {
                startDate = now.plusDays(10);
                endDate = now.plusDays(15);
            }
            case ENDED -> {
                startDate = now.minusDays(10);
                endDate = now.minusDays(5);
            }
            default -> {
                startDate = now;
                endDate = now;
            }
        }

        ClubEvent clubEvent = ClubEvent.builder()
            .id(eventId)
            .club(club)
            .name("B-CON " + eventId)
            .startDate(startDate)
            .endDate(endDate)
            .introduce("BCSDLab의 멘토 혹은 레귤러들의 경험을 공유해요.")
            .content("여러 동아리원들과 자신의 생각, 경험에 대해 나눠요.")
            .notifiedBeforeOneHour(false)
            .build();

        ClubEventImage clubEventImage = 동아리_행사_이미지(clubEvent);

        ReflectionTestUtils.setField(clubEvent, "images", List.of(clubEventImage));
        ReflectionTestUtils.setField(clubEvent, "createdAt", createdAt);

        return clubEvent;
    }

    private ClubEventImage 동아리_행사_이미지(ClubEvent clubEvent) {
        return ClubEventImage.builder()
            .clubEvent(clubEvent)
            .imageUrl("img")
            .build();
    }
}
