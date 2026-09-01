package in.koreatech.koin.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.ALREADY_APPLIED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.DEADLINE_PASSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.LOGIN_REQUIRED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.OWN_RECRUITMENT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.PROFILE_REQUIRED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.RECRUITMENT_CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.RECRUITMENT_DELETED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.ROLE_CLOSED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.team.recruitment.dto.CreatedRecruitment;
import in.koreatech.koin.domain.team.recruitment.dto.CreatedRecruitmentListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentCard;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentCards;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentDetail;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentListResponse;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentSort;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatusFilter;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentListQueryRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentQueryService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TeamRecruitmentRepository teamRecruitmentRepository;
    private final TeamRecruitmentListQueryRepository listQueryRepository;
    private final TeamRecruitmentApplicationRepository applicationRepository;
    private final TeamRecruitmentProfileRepository profileRepository;
    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final Clock clock;

    public RecruitmentListResponse getRecruitments(
        String keyword,
        TeamRecruitmentStatusFilter statusFilter,
        List<TeamRecruitmentCategory> categories,
        TeamRecruitmentMeetingType meetingType,
        TeamRecruitmentSort sort,
        Integer page,
        Integer limit
    ) {
        long total = listQueryRepository.count(keyword, statusFilter, categories, meetingType);
        Criteria criteria = Criteria.of(page, limit, (int) total);
        List<TeamRecruitment> recruitments =
            listQueryRepository.findAll(keyword, statusFilter, categories, meetingType, sort, criteria);
        return RecruitmentListResponse.of(pageOf(recruitments, criteria, total), criteria, today());
    }

    public CreatedRecruitmentListResponse getMyCreatedRecruitments(
        Integer userId,
        TeamRecruitmentStatusFilter statusFilter,
        TeamRecruitmentSort sort,
        Integer page,
        Integer limit
    ) {
        long total = listQueryRepository.countByAuthor(userId, statusFilter);
        Criteria criteria = Criteria.of(page, limit, (int) total);
        List<TeamRecruitment> recruitments = listQueryRepository.findAllByAuthor(userId, statusFilter, sort, criteria);

        LocalDate today = today();
        List<CreatedRecruitment> created = recruitments.stream()
            .map(recruitment -> CreatedRecruitment.of(
                RecruitmentCards.of(recruitment, today),
                applicantCountOf(recruitment),
                recruitment.isRecruiting(),
                teamChatRoomIdOf(recruitment.getId())
            ))
            .toList();
        return CreatedRecruitmentListResponse.of(created, pageOf(recruitments, criteria, total), criteria);
    }

    public RecruitmentDetail getRecruitment(Integer recruitmentId, Integer userId) {
        TeamRecruitment recruitment = teamRecruitmentRepository.findById(recruitmentId)
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_NOT_FOUND, "recruitmentId: " + recruitmentId));
        if (recruitment.isDeleted()) {
            throw CustomException.of(TEAM_RECRUITMENT_NOT_FOUND, "recruitmentId: " + recruitmentId);
        }
        LocalDate today = today();
        return toDetail(recruitment, userId, today);
    }

    private RecruitmentDetail toDetail(TeamRecruitment recruitment, Integer userId, LocalDate today) {
        RecruitmentCard card = RecruitmentCards.of(recruitment, today);
        boolean isAuthor = userId != null && recruitment.getAuthor().getId().equals(userId);
        Optional<TeamRecruitmentApplication> myApplication = userId == null
            ? Optional.empty()
            : applicationRepository.findByRecruitment_IdAndApplicant_Id(recruitment.getId(), userId);

        TeamRecruitmentApplyBlockReason blockReason = applyBlockReasonOf(
            recruitment,
            userId,
            isAuthor,
            myApplication,
            today
        );
        boolean accepted = myApplication
            .map(application -> application.getStatus() == ACCEPTED)
            .orElse(false);
        Integer teamChatRoomId = (isAuthor || accepted) ? teamChatRoomIdOf(recruitment.getId()) : null;

        return new RecruitmentDetail(
            card.id(),
            card.category(),
            card.title(),
            card.meetingType(),
            card.activityStartDate(),
            card.activityEndDate(),
            card.deadlineDate(),
            card.dDay(),
            card.status(),
            card.recruitmentType(),
            card.currentParticipants(),
            card.maxParticipants(),
            card.roles(),
            recruitment.getAuthor().getNickname(),
            recruitment.getDescription(),
            recruitment.getRelatedUrl(),
            recruitment.getQualification(),
            recruitment.getCreatedAt(),
            isAuthor,
            blockReason == null,
            blockReason,
            myApplication
                .map(application -> new RecruitmentDetail.AppliedApplication(
                    application.getId(), application.getStatus()))
                .orElse(null),
            isAuthor,
            teamChatRoomId != null,
            teamChatRoomId
        );
    }

    /**
     * 여러 사유가 동시에 성립하면 선언 순서가 이른 값을 반환한다. 지원 가능하면 null 이다.
     * 순서는 클라이언트와 합의한 화면 안내 우선순위를 따른다.
     */
    private TeamRecruitmentApplyBlockReason applyBlockReasonOf(
        TeamRecruitment recruitment,
        Integer userId,
        boolean isAuthor,
        Optional<TeamRecruitmentApplication> myApplication,
        LocalDate today
    ) {
        if (recruitment.isDeleted()) {
            return RECRUITMENT_DELETED;
        }
        if (userId == null) {
            return LOGIN_REQUIRED;
        }
        if (isAuthor) {
            return OWN_RECRUITMENT;
        }
        if (myApplication.isPresent()) {
            return ALREADY_APPLIED;
        }
        if (!recruitment.isRecruiting()) {
            return RECRUITMENT_CLOSED;
        }
        if (today.isAfter(recruitment.getDeadlineDate())) {
            return DEADLINE_PASSED;
        }
        if (isFullyClosed(recruitment)) {
            return ROLE_CLOSED;
        }
        if (!profileRepository.existsByUser_Id(userId)) {
            return PROFILE_REQUIRED;
        }
        return null;
    }

    /**
     * 역할 구분 모집은 모든 역할이 마감되면, 역할 구분이 없는 모집은 전체 정원이 차면 지원할 수 없다.
     */
    private boolean isFullyClosed(TeamRecruitment recruitment) {
        List<TeamRecruitmentRole> roles = recruitment.getRoles();
        if (roles.isEmpty()) {
            return recruitment.getCurrentParticipants() >= recruitment.getMaxParticipants();
        }
        return roles.stream().allMatch(TeamRecruitmentRole::isClosed);
    }

    private long applicantCountOf(TeamRecruitment recruitment) {
        return applicationRepository.countByRecruitment_IdAndStatusIn(recruitment.getId(), List.of(PENDING, ACCEPTED));
    }

    private Integer teamChatRoomIdOf(Integer recruitmentId) {
        return chatRoomRepository
            .findByRecruitment_IdAndRoomScopeKey(recruitmentId, TeamRecruitmentChatRoom.TEAM_ROOM_SCOPE_KEY)
            .map(TeamRecruitmentChatRoom::getId)
            .orElse(null);
    }

    private PageImpl<TeamRecruitment> pageOf(List<TeamRecruitment> content, Criteria criteria, long total) {
        return new PageImpl<>(content, PageRequest.of(criteria.getPage(), criteria.getLimit()), total);
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(KST));
    }
}
