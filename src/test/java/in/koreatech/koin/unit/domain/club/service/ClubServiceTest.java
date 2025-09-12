package in.koreatech.koin.unit.domain.club.service;

import in.koreatech.koin.common.event.ClubCreateEvent;
import in.koreatech.koin.common.event.ClubRecruitmentChangeEvent;
import in.koreatech.koin.domain.club.category.model.ClubCategory;
import in.koreatech.koin.domain.club.category.repository.ClubCategoryRepository;
import in.koreatech.koin.domain.club.club.dto.request.ClubCreateRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubIntroductionUpdateRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.club.dto.request.ClubUpdateRequest;
import in.koreatech.koin.domain.club.club.dto.response.ClubHotResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubRelatedKeywordResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubResponse;
import in.koreatech.koin.domain.club.club.dto.response.ClubsByCategoryResponse;
import in.koreatech.koin.domain.club.club.model.*;
import in.koreatech.koin.domain.club.club.repository.*;
import in.koreatech.koin.domain.club.event.repository.ClubEventImageRepository;
import in.koreatech.koin.domain.club.event.repository.ClubEventRepository;
import in.koreatech.koin.domain.club.event.repository.ClubEventSubscriptionRepository;
import in.koreatech.koin.domain.club.recruitment.enums.ClubRecruitmentStatus;
import in.koreatech.koin.domain.club.club.enums.ClubSortType;
import in.koreatech.koin.domain.club.club.enums.SNSType;
import in.koreatech.koin.domain.club.club.exception.ClubHotNotFoundException;
import in.koreatech.koin.domain.club.like.exception.ClubLikeDuplicateException;
import in.koreatech.koin.domain.club.like.exception.ClubLikeNotFoundException;
import in.koreatech.koin.domain.club.club.exception.ClubManagerAlreadyException;
import in.koreatech.koin.domain.club.like.model.ClubLike;
import in.koreatech.koin.domain.club.like.repository.ClubLikeRepository;
import in.koreatech.koin.domain.club.club.model.redis.ClubCreateRedis;
import in.koreatech.koin.domain.club.club.model.redis.ClubHotRedis;
import in.koreatech.koin.domain.club.qna.dto.request.ClubQnaCreateRequest;
import in.koreatech.koin.domain.club.qna.dto.response.ClubQnasResponse;
import in.koreatech.koin.domain.club.qna.model.ClubQna;
import in.koreatech.koin.domain.club.qna.repository.ClubQnaRepository;
import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.recruitment.model.ClubRecruitment;
import in.koreatech.koin.domain.club.recruitment.repository.ClubRecruitmentRepository;
import in.koreatech.koin.domain.club.recruitment.repository.ClubRecruitmentSubscriptionRepository;
import in.koreatech.koin.domain.club.club.repository.redis.ClubCreateRedisRepository;
import in.koreatech.koin.domain.club.club.repository.redis.ClubHitsRedisRepository;
import in.koreatech.koin.domain.club.club.repository.redis.ClubHotRedisRepository;
import in.koreatech.koin.domain.club.club.service.ClubService;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClubServiceTest {

    @Mock
    private ClubHotRedisRepository hotClubRedisRepository;

    @Mock
    private ClubHotRepository clubHotRepository;

    @Mock
    private ClubQnaRepository clubQnaRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClubManagerRepository clubManagerRepository;

    @Mock
    private ClubCategoryRepository clubCategoryRepository;

    @Mock
    private ClubSNSRepository clubSNSRepository;

    @Mock
    private ClubLikeRepository clubLikeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClubCreateRedisRepository clubCreateRedisRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ClubHitsRedisRepository clubHitsRedisRepository;

    @Mock
    private ClubRecruitmentRepository clubRecruitmentRepository;

    @Mock
    private ClubListQueryRepository clubListQueryRepository;

    @Mock
    private ClubRecruitmentSubscriptionRepository clubRecruitmentSubscriptionRepository;

    @Mock
    private ClubEventRepository clubEventRepository;

    @Mock
    private ClubEventImageRepository clubEventImageRepository;

    @Mock
    private ClubEventSubscriptionRepository clubEventSubscriptionRepository;

    @InjectMocks private ClubService clubService;

    @Nested
    class CreateClubRequest {

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
            ArgumentCaptor<ClubCreateEvent> eventCaptor = ArgumentCaptor.forClass(ClubCreateEvent.class);

            verify(clubCreateRedisRepository).save(redisCaptor.capture());
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            ClubCreateRedis createRedis = redisCaptor.getValue();
            ClubCreateEvent createEvent = eventCaptor.getValue();

            assertThat(createRedis.getName()).isEqualTo(request.name());
            assertThat(createRedis.getImageUrl()).isEqualTo(request.imageUrl());
            assertThat(createRedis.getClubAdmins()).isEqualTo(request.clubManagers());
            assertThat(createRedis.getClubCategoryId()).isEqualTo(request.clubCategoryId());
            assertThat(createRedis.getLocation()).isEqualTo(request.location());
            assertThat(createRedis.getDescription()).isEqualTo(request.description());
            assertThat(createRedis.getInstagram()).isEqualTo(request.instagram());
            assertThat(createRedis.getGoogleForm()).isEqualTo(request.googleForm());
            assertThat(createRedis.getOpenChat()).isEqualTo(request.openChat());
            assertThat(createRedis.getPhoneNumber()).isEqualTo(request.phoneNumber());
            assertThat(createRedis.getRequesterId()).isEqualTo(studentId);
            assertThat(createRedis.getRole()).isEqualTo(request.role());
            assertThat(createRedis.getIsLikeHidden()).isEqualTo(request.isLikeHidden());

            assertThat(createEvent.clubName()).isEqualTo(request.name());
        }
    }

    @Nested
    class UpdateClub {

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

            newCategory = ClubCategoryFixture.동아리_카테고리(2, "문화");

            clubId = 1;
            studentId = 1;
            club = ClubFixture.활성화_BCSD_동아리(clubId);

            when(clubRepository.getById(clubId)).thenReturn(club);
        }

        @Test
        void 동아리_관리자가_정보_수정_요청을_보낸_경우_정상적으로_처리한다() {
            // given
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
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubService.updateClub(clubId, request, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("권한이 없습니다.");
        }
    }

    @Nested
    class UpdateClubIntroduction {

        ClubIntroductionUpdateRequest request;
        Integer clubId;
        Integer studentId;
        Club club;

        @BeforeEach
        void init() {
            request = new ClubIntroductionUpdateRequest("수정된 동아리 소개 문자열");
            clubId = 1;
            studentId = 1;
            club = ClubFixture.활성화_BCSD_동아리(clubId);

            // 공통 stub
            when(clubRepository.getById(clubId)).thenReturn(club);
        }

        @Test
        void 동아리_관리자가_동아리_소개_수정_요청을_보낸_경우_정상적으로_처리한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubLikeRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubRecruitmentSubscriptionRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);

            // when
            ClubResponse response = clubService.updateClubIntroduction(clubId, request, studentId);

            // then / 소개가 정상적으로 바뀌었는 지 확인
            assertThat(response.introduction()).isEqualTo(request.introduction());

            // then / 그 이외 필드에서 변경된 값이 있는 지 확인
            assertThat(response.id()).isEqualTo(club.getId());
            assertThat(response.name()).isEqualTo(club.getName());
            assertThat(response.category()).isEqualTo(club.getClubCategory().getName());
            assertThat(response.location()).isEqualTo(club.getLocation());
            assertThat(response.imageUrl()).isEqualTo(club.getImageUrl());
            assertThat(response.likes()).isEqualTo(club.getLikes());
            assertThat(response.description()).isEqualTo(club.getDescription());
            assertThat(response.instagram()).isEqualTo(getSNSUrl(club, SNSType.INSTAGRAM));
            assertThat(response.googleForm()).isEqualTo(getSNSUrl(club, SNSType.GOOGLE_FORM));
            assertThat(response.openChat()).isEqualTo(getSNSUrl(club, SNSType.OPEN_CHAT));
            assertThat(response.phoneNumber()).isEqualTo(getSNSUrl(club, SNSType.PHONE_NUMBER));
        }

        Optional<String> getSNSUrl(Club club, SNSType type) {
            return club.getClubSNSs().stream()
                .filter(sns -> sns.getSnsType().equals(type))
                .map(ClubSNS::getContact)
                .findFirst();
        }

        @Test
        void 동아리_관리자가_아닌_유저가_동아리_소개_수정_요청을_보낸_경우_예외를_발생한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubService.updateClubIntroduction(clubId, request, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("권한이 없습니다.");
        }
    }

    @Nested
    class GetClub {

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
            Club club = ClubFixture.활성화_BCSD_동아리(clubId);

            List<ClubSNS> snsList = List.of(
                new ClubSNS(club, SNSType.INSTAGRAM, "https://instagram.com/bcsdlab")
            );

            ClubHot clubHot1 = ClubHotFixture.인기_동아리(1, club);
            ClubHot clubHot2 = ClubHotFixture.인기_동아리(2, club);

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
            assertThat(response.hotStatus().month()).isEqualTo(LocalDate.now().getMonthValue());
            assertThat(response.hotStatus().weekOfMonth()).isEqualTo(calculateWeekOfMonth(clubHot2.getStartDate()));
            assertThat(response.hotStatus().streakCount()).isEqualTo(2);

            verify(clubHitsRedisRepository).incrementHits(clubId);
        }

        int calculateWeekOfMonth(LocalDate startDate) {
            return ((startDate.getDayOfMonth() - 1) / 7) + 1;
        }

        @Test
        void 비활성화된_동아리를_상세_조회_시_예외를_발생한다() {
            // given
            Club club = ClubFixture.비활성화_BCSD_동아리(clubId);
            when(clubRepository.getById(clubId)).thenReturn(club);

            // when / then
            assertThatThrownBy(() -> clubService.getClub(clubId, studentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("비활성화 동아리입니다.");
        }
    }

    @Nested
    class GetClubByCategory {

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

    @Nested
    class GetRelatedClubs {

        String emptyQuery;
        String query;
        Club club;
        PageRequest pageRequest;

        @BeforeEach
        void init() {
            emptyQuery = "";
            query = "Bc";
            club = ClubFixture.활성화_BCSD_동아리(1);
            pageRequest = PageRequest.of(0, getRelatedLimitSize());
        }

        @Test
        void 키워드_내용의_접두사를_가진_동아리_리스트를_반환한다() {
            // given
            when(clubRepository.findByNamePrefix(getNormalizeString(query), pageRequest)).thenReturn(List.of(club));

            // when
            ClubRelatedKeywordResponse response = clubService.getRelatedClubs(query);

            // then
            assertThat(response.keywords()).hasSize(1);
            assertThat(response.keywords().get(0).clubId()).isEqualTo(club.getId());
            assertThat(response.keywords().get(0).clubName()).isEqualTo(club.getName());
        }

        @Test
        void 검색_키워드가_비어있으면_빈_리스트를_반환한다() {
            // when
            ClubRelatedKeywordResponse response = clubService.getRelatedClubs(emptyQuery);

            // then
            assertThat(response.keywords()).hasSize(0);
        }

        int getRelatedLimitSize() {
            try {
                Field field = ClubService.class.getDeclaredField("RELATED_LIMIT_SIZE");
                field.setAccessible(true);

                return (int) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("RELATED_LIMIT_SIZE 값을 가져올 수 없습니다.", e);
            }
        }

        String getNormalizeString(String s) {
            return s.replaceAll("\\s+", "").toLowerCase();
        }
    }

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
            ClubQnasResponse response = clubService.getQnas(clubId);

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
    class GetHotClub {

        Integer clubHotId;
        Integer clubId;
        ClubHotRedis clubHotRedis;

        @BeforeEach
        void init() {
            clubHotId = 1;
            clubId = 1;
        }

        @Test
        void 캐시_히트_시_캐시에_저장된_값을_반환한다() {
            // given
            clubHotRedis = ClubHotFixture.인기_동아리_레디스(clubHotId, clubId);

            when(hotClubRedisRepository.findById(ClubHotRedis.REDIS_KEY)).thenReturn(Optional.of(clubHotRedis));

            // when
            ClubHotResponse response = clubService.getHotClub();

            // then
            assertThat(response.clubId()).isEqualTo(clubId);
            assertThat(response.name()).isEqualTo("BCSD Lab");
            assertThat(response.imageUrl()).isEqualTo("https://bcsdlab.com/static/img/logo.d89d9cc.png");
        }

        @Test
        void 캐시_미스_시_DB에_저장된_값을_조회_후_캐싱하여_반환한다() {
            // given
            Club club = ClubFixture.활성화_BCSD_동아리(clubId);
            ClubHot clubHot = ClubHotFixture.인기_동아리(clubHotId, club);

            when(hotClubRedisRepository.findById(ClubHotRedis.REDIS_KEY)).thenReturn(Optional.empty());

            when(clubHotRepository.findTopByOrderByEndDateDesc()).thenReturn(Optional.of(clubHot));

            // when
            ClubHotResponse response = clubService.getHotClub();

            // then
            verify(hotClubRedisRepository).findById(ClubHotRedis.REDIS_KEY);
            verify(clubHotRepository).findTopByOrderByEndDateDesc();
            verify(hotClubRedisRepository).save(any(ClubHotRedis.class));

            assertThat(response.clubId()).isEqualTo(clubId);
            assertThat(response.name()).isEqualTo("BCSD Lab");
            assertThat(response.imageUrl()).isEqualTo("https://bcsdlab.com/static/img/logo.d89d9cc.png");
        }

        @Test
        void 캐시와_DB에_모두_없으면_예외를_발생한다() {
            // given
            when(hotClubRedisRepository.findById(ClubHotRedis.REDIS_KEY)).thenReturn(Optional.empty());

            when(clubHotRepository.findTopByOrderByEndDateDesc()).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> clubService.getHotClub())
                .isInstanceOf(ClubHotNotFoundException.class)
                .hasMessage("인기 동아리가 존재하지 않습니다.");
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
            clubService.createQna(question, clubId, studentId);

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
            clubService.createQna(answer, clubId, studentId);

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
            assertThatThrownBy(() -> clubService.createQna(answer, clubId, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }

        @Test
        void 관리자인_학생이_질문을_작성하면_예외를_발생한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> clubService.createQna(question, clubId, studentId))
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
            clubService.deleteQna(clubId, qnaId, studentId);

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
            clubService.deleteQna(clubId, qnaId, managerId);

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
            assertThatThrownBy(() -> clubService.deleteQna(clubId, qnaId, requesterId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");

            verify(clubQna, never()).detachFromParentIfChild();
            verify(clubQnaRepository, never()).delete(clubQna);
        }
    }

    @Nested
    class EmpowermentClubManager {

        Integer clubId;
        Integer currentManagerId;
        Integer changedManagerId;
        Club club;
        User currentManager;
        User changedManager;
        ClubManagerEmpowermentRequest request;

        @BeforeEach
        void init() {
            clubId = 1;
            currentManagerId = 1;
            changedManagerId = 2;
            club = ClubFixture.활성화_BCSD_동아리(clubId);
            currentManager = UserFixture.코인_유저();
            changedManager = UserFixture.코인_유저();
            request = new ClubManagerEmpowermentRequest(clubId, changedManager.getLoginId());

            ReflectionTestUtils.setField(currentManager, "id", currentManagerId);
            ReflectionTestUtils.setField(changedManager, "id", changedManagerId);

            when(clubRepository.getById(clubId)).thenReturn(club);
            when(userRepository.getById(currentManagerId)).thenReturn(currentManager);
            when(userRepository.getByLoginIdAndUserTypeIn(changedManager.getLoginId(), UserType.KOIN_STUDENT_TYPES))
                .thenReturn(changedManager);
        }

        @Test
        void 관리자_권한_위임에_성공한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, currentManagerId)).thenReturn(true);
            when(clubManagerRepository.existsByClubAndUser(club, changedManager)).thenReturn(false);

            // when
            clubService.empowermentClubManager(request, currentManagerId);

            // then
            ArgumentCaptor<ClubManager> clubManagerCaptor = ArgumentCaptor.forClass(ClubManager.class);

            verify(clubManagerRepository).deleteByClubAndUser(club, currentManager);
            verify(clubManagerRepository).save(clubManagerCaptor.capture());

            ClubManager newClubManager = clubManagerCaptor.getValue();

            assertThat(newClubManager.getClub()).isEqualTo(club);
            assertThat(newClubManager.getUser()).isEqualTo(changedManager);
        }

        @Test
        void 관리자가_아닌_유저가_권한_위임을_할_경우_예외를_발생한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, currentManagerId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubService.empowermentClubManager(request, currentManagerId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }

        @Test
        void 권한_위임_대상이_이미_관리자면_예외를_발생한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, currentManagerId)).thenReturn(true);
            when(clubManagerRepository.existsByClubAndUser(club, changedManager)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> clubService.empowermentClubManager(request, currentManagerId))
                .isInstanceOf(ClubManagerAlreadyException.class)
                .hasMessage("이미 동아리의 관리자입니다.");
        }
    }

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
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubRecruitmentRepository.findByClub(club)).thenReturn(Optional.empty());

            // when
            clubService.createRecruitment(request, clubId, studentId);

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
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> clubService.createRecruitment(request, clubId, studentId))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("권한이 없습니다.");
        }

        @Test
        void 동아리_모집이_이미_등록되어_있으면_예외를_발생한다() {
            // given
            when(clubManagerRepository.existsByClubIdAndUserId(clubId, studentId)).thenReturn(true);
            when(clubRecruitmentRepository.findByClub(club)).thenReturn(Optional.of(ClubRecruitment.builder().build()));

            // when / then
            assertThatThrownBy(() -> clubService.createRecruitment(request, clubId, studentId))
                .isInstanceOf(CustomException.class)
                .hasMessage("동아리 공고가 이미 존재합니다.");
        }
    }
}
