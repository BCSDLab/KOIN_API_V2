package in.koreatech.koin.unit.domain.club.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import in.koreatech.koin.common.event.ClubCreateEvent;
import in.koreatech.koin.domain.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.enums.ClubRecruitmentStatus;
import in.koreatech.koin.domain.club.enums.ClubSortType;
import in.koreatech.koin.domain.club.enums.SNSType;
import in.koreatech.koin.domain.club.model.Club;
import in.koreatech.koin.domain.club.model.ClubBaseInfo;
import in.koreatech.koin.domain.club.model.ClubCategory;
import in.koreatech.koin.domain.club.model.ClubHot;
import in.koreatech.koin.domain.club.model.ClubSNS;
import in.koreatech.koin.domain.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.club.repository.ClubCategoryRepository;
import in.koreatech.koin.domain.club.repository.ClubEventImageRepository;
import in.koreatech.koin.domain.club.repository.ClubEventRepository;
import in.koreatech.koin.domain.club.repository.ClubEventSubscriptionRepository;
import in.koreatech.koin.domain.club.repository.ClubHotRepository;
import in.koreatech.koin.domain.club.repository.ClubLikeRepository;
import in.koreatech.koin.domain.club.repository.ClubListQueryRepository;
import in.koreatech.koin.domain.club.repository.ClubManagerRepository;
import in.koreatech.koin.domain.club.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.repository.ClubRecruitmentRepository;
import in.koreatech.koin.domain.club.repository.ClubRecruitmentSubscriptionRepository;
import in.koreatech.koin.domain.club.repository.ClubRepository;
import in.koreatech.koin.domain.club.repository.ClubSNSRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubCreateRedisRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHitsRedisRepository;
import in.koreatech.koin.domain.club.repository.redis.ClubHotRedisRepository;
import in.koreatech.koin.domain.club.service.ClubService;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.ClubBaseInfoFixture;
import in.koreatech.koin.unit.fixture.ClubFixture;

@ExtendWith(MockitoExtension.class)
public class ClubServiceTest {

    @Mock private ClubHotRedisRepository hotClubRedisRepository;
    @Mock private ClubHotRepository clubHotRepository;
    @Mock private ClubQnaRepository clubQnaRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private ClubManagerRepository clubManagerRepository;
    @Mock private ClubCategoryRepository clubCategoryRepository;
    @Mock private ClubSNSRepository clubSNSRepository;
    @Mock private ClubLikeRepository clubLikeRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClubCreateRedisRepository clubCreateRedisRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ClubHitsRedisRepository clubHitsRedisRepository;
    @Mock private ClubRecruitmentRepository clubRecruitmentRepository;
    @Mock private ClubListQueryRepository clubListQueryRepository;
    @Mock private ClubRecruitmentSubscriptionRepository clubRecruitmentSubscriptionRepository;
    @Mock private ClubEventRepository clubEventRepository;
    @Mock private ClubEventImageRepository clubEventImageRepository;
    @Mock private ClubEventSubscriptionRepository clubEventSubscriptionRepository;

    @InjectMocks private ClubService clubService;

    @Nested
    class createClubRequest {

        ClubCreateRequest request;
        Integer studentId;

        @BeforeEach
        void init() {
            request = new ClubCreateRequest(
                "BCSD",
                "https://bcsdlab.com/static/img/logo.d89d9cc.png",
                List.of(new ClubCreateRequest.InnerClubManagerRequest("bcsdlab")),
                1,
                "학생회관",
                "즐겁게 일하고 열심히 노는 IT 특성화 동아리",
                "https://www.instagram.com/bcsdlab/",
                "https://forms.gle/example",
                "https://open.kakao.com/example",
                "01012345678",
                "회장",
                false
            );
            studentId = 1;
        }

        @Test
        void 동아리_생성_요청을_처리한다() {
            // when
            clubService.createClubRequest(request, studentId);

            // then
            ArgumentCaptor<ClubCreateRedis> redisCaptor = ArgumentCaptor.forClass(ClubCreateRedis.class);
            verify(clubCreateRedisRepository).save(redisCaptor.capture());

            ArgumentCaptor<ClubCreateEvent> eventCaptor = ArgumentCaptor.forClass(ClubCreateEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
        }
    }

    @Nested
    class updateClub {

        ClubUpdateRequest request;
        ClubCategory newCategory;
        Integer clubId;
        Integer studentId;
        Club club;

        @BeforeEach
        void init() {
            request = new ClubUpdateRequest(
                "Updated BCSD",
                "https://bcsdlab.com/static/img/new_logo.png",
                2,
                "2공학관",
                "즐겁게 일하고 열심히 노는 IT 특성화 동아리",
                "https://www.instagram.com/bcsdlab/",
                "https://forms.gle/example",
                "https://open.kakao.com/example",
                "01012345678",
                false
            );

            newCategory = ClubCategory.builder()
                .id(2)
                .name("문화")
                .build();

            clubId = 1;
            studentId = 1;
            club = ClubFixture.활성화_BCSD_동아리(1);
        }

        @Test
        void 동아리_관리자가_정보_수정_요청을_보낸_경우_정상적으로_처리한다() {
            // given
            when(clubRepository.getById(clubId)).thenReturn(club);
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubCategoryRepository.getById(request.clubCategoryId())).thenReturn(newCategory);
            when(clubLikeRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubRecruitmentSubscriptionRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);

            // when
            ClubResponse response = clubService.updateClub(clubId, request, studentId);

            // then (club 객체 상태 검증)
            assertThat(club.getName()).isEqualTo(request.name());
            assertThat(club.getImageUrl()).isEqualTo(request.imageUrl());
            assertThat(club.getClubCategory()).isEqualTo(newCategory);
            assertThat(club.getLocation()).isEqualTo(request.location());
            assertThat(club.getDescription()).isEqualTo(request.description());
            assertThat(club.getIsLikeHidden()).isEqualTo(request.isLikeHidden());

            // then (response 상태 검증)
            assertThat(response.name()).isEqualTo(request.name());
            assertThat(response.imageUrl()).isEqualTo(request.imageUrl());
            assertThat(response.category()).isEqualTo(newCategory.getName());
            assertThat(response.location()).isEqualTo(request.location());
            assertThat(response.description()).isEqualTo(request.description());
            assertThat(response.isLikeHidden()).isEqualTo(request.isLikeHidden());
            assertThat(response.isLiked()).isTrue();
            assertThat(response.isRecruitSubscribed()).isTrue();
            assertThat(response.manager()).isTrue();
            assertThat(response.instagram()).contains(request.instagram());
            assertThat(response.googleForm()).contains(request.googleForm());
            assertThat(response.openChat()).contains(request.openChat());
            assertThat(response.phoneNumber()).contains(request.phoneNumber());
        }

        @Test
        void 동아리_관리자가_아닌_유저가_정보_수정_요청을_보낸_경우_예외를_발생한다() {
            // given
            when(clubRepository.getById(clubId)).thenReturn(club);
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubService.updateClub(clubId, request, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("권한이 없습니다.");
        }
    }

    @Nested
    class updateClubIntroduction {

        ClubIntroductionUpdateRequest request;
        Integer clubId;
        Integer studentId;
        Club club;

        @BeforeEach
        void init() {
            request = new ClubIntroductionUpdateRequest("수정된 동아리 소개 문자열");
            clubId = 1;
            studentId = 1;
            club = ClubFixture.활성화_BCSD_동아리(1);
        }

        @Test
        void 동아리_관리자가_동아리_소개_수정_요청을_보낸_경우_정상적으로_처리한다() {
            // given
            when(clubRepository.getById(clubId)).thenReturn(club);
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubLikeRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubRecruitmentSubscriptionRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);

            // when
            ClubResponse response = clubService.updateClubIntroduction(clubId, request, studentId);

            // then
            assertThat(response.introduction()).isEqualTo(request.introduction());
        }

        @Test
        void 동아리_관리자가_아닌_유저가_동아리_소개_수정_요청을_보낸_경우_예외를_발생한다() {
            // given
            when(clubRepository.getById(clubId)).thenReturn(club);
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubService.updateClubIntroduction(clubId, request, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("권한이 없습니다.");
        }
    }

    @Nested
    class getClub {

        Integer clubId;
        Integer studentId;

        @BeforeEach
        void init() {
            clubId = 1;
            studentId = 1;
        }

        @Test
        void 활성화된_동아리를_상세_조회한다() {
            // given
            Club club = ClubFixture.활성화_BCSD_동아리(1);

            List<ClubSNS> snsList = List.of(
                new ClubSNS(club, SNSType.INSTAGRAM, "https://instagram.com/bcsdlab")
            );

            ClubHot clubHot1 = ClubHot.builder()
                .id(1)
                .club(club)
                .ranking(1)
                .periodHits(100)
                .startDate(LocalDate.of(2025, 8, 21))
                .endDate(LocalDate.of(2025, 8, 21))
                .build();

            ClubHot clubHot2 = ClubHot.builder()
                .id(2)
                .club(club)
                .ranking(1)
                .periodHits(150)
                .startDate(LocalDate.of(2025, 8, 28))
                .endDate(LocalDate.of(2025, 8, 28))
                .build();

            when(clubRepository.getById(clubId)).thenReturn(club);
            when(clubSNSRepository.findAllByClub(club)).thenReturn(snsList);
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubLikeRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubRecruitmentSubscriptionRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubHotRepository.findTopByClubIdOrderByIdDesc(clubId)).thenReturn(Optional.of(clubHot2));
            when(clubHotRepository.findAllByOrderByIdDesc()).thenReturn(List.of(clubHot2, clubHot1));

            // when
            ClubResponse response = clubService.getClub(clubId, studentId);

            // then
            assertThat(response.name()).isEqualTo(club.getName());
            assertThat(response.manager()).isTrue();
            assertThat(response.isLiked()).isTrue();
            assertThat(response.isRecruitSubscribed()).isTrue();

            assertThat(response.hotStatus()).isNotNull();
            assertThat(response.hotStatus().month()).isEqualTo(8);
            assertThat(response.hotStatus().weekOfMonth()).isEqualTo(4);
            assertThat(response.hotStatus().streakCount()).isEqualTo(2);

            verify(clubHitsRedisRepository).incrementHits(clubId);
        }

        @Test
        void 비활성화된_동아리를_상세_조회_시_예외를_발생한다() {
            // given
            Club club = ClubFixture.비활성화_BCSD_동아리(1);
            when(clubRepository.getById(clubId)).thenReturn(club);

            // when / then
            assertThatThrownBy(() -> clubService.getClub(clubId, studentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("비활성화 동아리입니다.");
        }
    }

    @Nested
    class getClubByCategory {

        Integer categoryId;
        Boolean isRecruiting;
        ClubSortType sortType;
        String query;
        Integer userId;

        @BeforeEach
        void init() {
            categoryId = null;
            isRecruiting = false;
            sortType = ClubSortType.CREATED_AT_ASC;
            query = null;
            userId = 1;
        }

        @Test
        void 모든_동아리를_조회한다() {
            // given
            List<ClubBaseInfo> clubBaseInfos = List.of(
                ClubBaseInfoFixture.동아리_기본_정보(1, "테스트1", "학술", ClubRecruitmentStatus.RECRUITING),
                ClubBaseInfoFixture.동아리_기본_정보(2, "테스트2", "운동", ClubRecruitmentStatus.ALWAYS),
                ClubBaseInfoFixture.동아리_기본_정보(3, "테스트3", "공연", ClubRecruitmentStatus.NONE)
            );

            when(clubListQueryRepository.findAllClubInfo(categoryId, sortType, isRecruiting, query, userId))
                .thenReturn(clubBaseInfos);

            // when
            ClubsByCategoryResponse response = clubService.getClubByCategory(categoryId, isRecruiting, sortType, query, userId);

            // then
            assertThat(response.clubs()).hasSize(3);
        }

        @Test
        void 특정_카테고리를_지닌_모든_동아리를_조회한다() {
            // given
            List<ClubBaseInfo> clubBaseInfos = List.of(
                ClubBaseInfoFixture.동아리_기본_정보(1, "테스트1", "학술", ClubRecruitmentStatus.RECRUITING)
            );

            categoryId = 1;
            isRecruiting = false;
            sortType = ClubSortType.CREATED_AT_ASC;
            query = null;
            userId = 1;

            when(clubListQueryRepository.findAllClubInfo(categoryId, sortType, isRecruiting, query, userId))
                .thenReturn(clubBaseInfos);

            // when
            ClubsByCategoryResponse response = clubService.getClubByCategory(
                categoryId, isRecruiting, sortType, query, userId
            );

            // then
            assertThat(response.clubs())
                .hasSize(1)
                .extracting("category")
                .containsExactly("학술");
        }

        @Test
        void query_내용을_지닌_모든_동아리를_조회한다() {
            // given
            List<ClubBaseInfo> clubBaseInfos = List.of(
                ClubBaseInfoFixture.동아리_기본_정보(1, "BCSD Lab", "학술", ClubRecruitmentStatus.RECRUITING)
            );

            query = "Lab";

            when(clubListQueryRepository.findAllClubInfo(categoryId, sortType, isRecruiting, query, userId))
                .thenReturn(clubBaseInfos);

            // when
            ClubsByCategoryResponse response = clubService.getClubByCategory(
                categoryId, isRecruiting, sortType, query, userId
            );

            // then
            assertThat(response.clubs())
                .hasSize(1)
                .extracting("name")
                .anyMatch(name -> ((String)name).toLowerCase().contains(query.toLowerCase()));
        }

        @Test
        void 인원_모집중인_모든_동아리를_조회한다() {
            // given
            List<ClubBaseInfo> clubBaseInfos = List.of(
                ClubBaseInfoFixture.동아리_기본_정보(1, "테스트1", "학술", ClubRecruitmentStatus.RECRUITING),
                ClubBaseInfoFixture.동아리_기본_정보(2, "테스트2", "학술", ClubRecruitmentStatus.ALWAYS)
            );

            isRecruiting = true;

            when(clubListQueryRepository.findAllClubInfo(categoryId, sortType, isRecruiting, query, userId))
                .thenReturn(clubBaseInfos);

            // when
            ClubsByCategoryResponse response = clubService.getClubByCategory(
                categoryId, isRecruiting, sortType, query, userId
            );

            // then
            assertThat(response.clubs())
                .hasSize(2)
                .extracting("recruitmentInfo")
                .extracting("status")
                .containsAnyOf("ALWAYS", "RECRUITING");
        }

        @Test
        void 동아리_모집_관련_정렬이지만_모집중이_아닌_옵션을_선택_시_예외를_발생한다() {
            // given
            isRecruiting = false;
            ClubSortType sortType1 = ClubSortType.RECRUITING_DEADLINE_ASC;
            ClubSortType sortType2 = ClubSortType.RECRUITMENT_UPDATED_DESC;

            // when / then
            assertThatThrownBy(() -> clubService.getClubByCategory(categoryId, isRecruiting, sortType1, query, userId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException)ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ApiResponseCode.NOT_ALLOWED_RECRUITING_SORT_TYPE);
                    assertThat(ce.getMessage()).contains("해당 정렬 방식은 모집 중일 때만 사용할 수 있습니다.");
                });

            assertThatThrownBy(() -> clubService.getClubByCategory(categoryId, isRecruiting, sortType2, query, userId))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException)ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ApiResponseCode.NOT_ALLOWED_RECRUITING_SORT_TYPE);
                    assertThat(ce.getMessage()).contains("해당 정렬 방식은 모집 중일 때만 사용할 수 있습니다.");
                });
        }
    }
}
