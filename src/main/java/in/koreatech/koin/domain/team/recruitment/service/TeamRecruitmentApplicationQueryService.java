package in.koreatech.koin.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.DIRECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicantDetail;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicantListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicantRecruitment;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicantSummary;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicationRole;
import in.koreatech.koin.domain.team.recruitment.dto.MyApplication;
import in.koreatech.koin.domain.team.recruitment.dto.MyApplicationListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.ProfileSnapshot;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentCard;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationSort;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentApplicationQueryService {

    private static final String TEAM_ROOM_SCOPE_KEY = "TEAM";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final List<TeamRecruitmentApplicationStatus> ALL_STATUSES = List.of(
        PENDING,
        ACCEPTED,
        TeamRecruitmentApplicationStatus.REJECTED
    );

    private final TeamRecruitmentRepository recruitmentRepository;
    private final TeamRecruitmentApplicationRepository applicationRepository;
    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final StudentRepository studentRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public MyApplicationListResponse getMyApplications(
        List<TeamRecruitmentApplicationStatus> statuses,
        TeamRecruitmentApplicationSort sort,
        Integer page,
        Integer limit,
        Integer studentId
    ) {
        studentRepository.getById(studentId);
        Collection<TeamRecruitmentApplicationStatus> selectedStatuses = normalizeStatuses(statuses);
        long totalCount = applicationRepository.countByApplicant_IdAndStatusIn(studentId, selectedStatuses);
        Criteria criteria = Criteria.of(page, limit, safeInt(totalCount));
        Page<TeamRecruitmentApplication> applications = applicationRepository.findAllByApplicant_IdAndStatusIn(
            studentId,
            selectedStatuses,
            pageRequest(criteria, sort, true)
        );
        AcceptedChatRooms acceptedChatRooms = findAcceptedChatRooms(applications.getContent());
        List<MyApplication> content = applications.getContent().stream()
            .map(application -> toMyApplication(application, acceptedChatRooms))
            .toList();
        return new MyApplicationListResponse(
            content,
            totalCount,
            content.size(),
            totalPage(totalCount, criteria.getLimit()),
            criteria.getPage() + 1
        );
    }

    public ApplicantListResponse getApplications(
        Integer recruitmentId,
        List<TeamRecruitmentApplicationStatus> statuses,
        Integer page,
        Integer limit,
        Integer authorId
    ) {
        studentRepository.getById(authorId);
        TeamRecruitment recruitment = getRecruitment(recruitmentId);
        validateAuthor(recruitment, authorId);
        if (recruitment.isDeleted()) {
            throw CustomException.of(TEAM_RECRUITMENT_NOT_FOUND);
        }

        Collection<TeamRecruitmentApplicationStatus> selectedStatuses = normalizeStatuses(statuses);
        long totalCount = applicationRepository.countByRecruitment_IdAndStatusIn(recruitmentId, selectedStatuses);
        Criteria criteria = Criteria.of(page, limit, safeInt(totalCount));
        Page<TeamRecruitmentApplication> applications = applicationRepository.findAllByRecruitment_IdAndStatusIn(
            recruitmentId,
            selectedStatuses,
            pageRequest(criteria, TeamRecruitmentApplicationSort.LATEST_DESC, false)
        );
        List<ApplicantSummary> content = applications.getContent().stream()
            .map(this::toApplicantSummary)
            .toList();
        return new ApplicantListResponse(
            toApplicantRecruitment(recruitment),
            content,
            totalCount,
            content.size(),
            totalPage(totalCount, criteria.getLimit()),
            criteria.getPage() + 1
        );
    }

    public ApplicantDetail getApplicationDetail(
        Integer recruitmentId,
        Integer applicationId,
        Integer authorId
    ) {
        studentRepository.getById(authorId);
        TeamRecruitment recruitment = getRecruitment(recruitmentId);
        validateAuthor(recruitment, authorId);
        if (recruitment.isDeleted()) {
            throw CustomException.of(TEAM_RECRUITMENT_NOT_FOUND);
        }
        TeamRecruitmentApplication application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_FOUND));
        validateApplicationBelongsToRecruitment(application, recruitmentId);

        boolean canDecide = application.getStatus() == PENDING
            && recruitment.getStatus() == RECRUITING
            && !isPastDeadline(recruitment);
        return new ApplicantDetail(
            application.getId(),
            application.getStatus(),
            readProfileSnapshot(application.getProfileSnapshot()),
            application.getMotivation(),
            application.getAvailability(),
            toApplicationRole(application.getRole()),
            canDecide,
            application.getStatus() == ACCEPTED
        );
    }

    private TeamRecruitment getRecruitment(Integer recruitmentId) {
        if (recruitmentId == null) {
            throw CustomException.of(TEAM_RECRUITMENT_NOT_FOUND);
        }
        return recruitmentRepository.findById(recruitmentId)
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_NOT_FOUND));
    }

    private void validateAuthor(TeamRecruitment recruitment, Integer authorId) {
        if (authorId == null || recruitment.getAuthor() == null
            || !Objects.equals(recruitment.getAuthor().getId(), authorId)) {
            throw CustomException.of(TEAM_RECRUITMENT_FORBIDDEN);
        }
    }

    private void validateApplicationBelongsToRecruitment(
        TeamRecruitmentApplication application,
        Integer recruitmentId
    ) {
        if (application.getRecruitment() == null
            || !Objects.equals(application.getRecruitment().getId(), recruitmentId)) {
            throw CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_FOUND);
        }
    }

    private Collection<TeamRecruitmentApplicationStatus> normalizeStatuses(
        List<TeamRecruitmentApplicationStatus> statuses
    ) {
        return statuses == null || statuses.isEmpty() ? ALL_STATUSES : statuses;
    }

    private PageRequest pageRequest(
        Criteria criteria,
        TeamRecruitmentApplicationSort sort,
        boolean includeRecruitmentSort
    ) {
        Sort ordering = sorting(sort, includeRecruitmentSort);
        return PageRequest.of(criteria.getPage(), criteria.getLimit(), ordering);
    }

    private Sort sorting(TeamRecruitmentApplicationSort sort, boolean includeRecruitmentSort) {
        if (sort == TeamRecruitmentApplicationSort.DEADLINE_ASC && includeRecruitmentSort) {
            return Sort.by(Sort.Direction.ASC, "recruitment.deadlineDate")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"))
                .and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return Sort.by(Sort.Direction.DESC, "createdAt")
            .and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private int totalPage(long totalCount, int limit) {
        if (totalCount == 0) {
            return 1;
        }
        return (int)Math.ceil((double)totalCount / limit);
    }

    private int safeInt(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)value;
    }

    private MyApplication toMyApplication(
        TeamRecruitmentApplication application,
        AcceptedChatRooms acceptedChatRooms
    ) {
        TeamRecruitment recruitment = application.getRecruitment();
        boolean accepted = application.getStatus() == ACCEPTED;
        TeamRecruitmentChatRoom teamRoom = accepted
            ? requireAcceptedTeamRoom(
                recruitment,
                application.getId(),
                acceptedChatRooms.teamRoomsByRecruitmentId().get(recruitment.getId())
            )
            : null;
        TeamRecruitmentChatRoom directRoom = accepted
            ? acceptedChatRooms.directRoomsByApplicationId().get(application.getId())
            : null;
        return new MyApplication(
            application.getId(),
            application.getStatus(),
            accepted && teamRoom != null,
            accepted && teamRoom != null ? teamRoom.getId() : null,
            directRoom == null ? null : directRoom.getId(),
            toApplicationRole(application.getRole()),
            toRecruitmentCard(recruitment)
        );
    }

    private TeamRecruitmentChatRoom requireAcceptedTeamRoom(
        TeamRecruitment recruitment,
        Integer applicationId,
        TeamRecruitmentChatRoom teamRoom
    ) {
        if (teamRoom == null) {
            throw new KoinIllegalStateException(
                "ACCEPTED 지원서에 TEAM 채팅방이 없습니다. recruitmentId: "
                    + recruitment.getId() + ", applicationId: " + applicationId
            );
        }
        return teamRoom;
    }

    private AcceptedChatRooms findAcceptedChatRooms(List<TeamRecruitmentApplication> applications) {
        List<TeamRecruitmentApplication> acceptedApplications = applications.stream()
            .filter(application -> application.getStatus() == ACCEPTED)
            .toList();
        if (acceptedApplications.isEmpty()) {
            return new AcceptedChatRooms(Map.of(), Map.of());
        }

        List<Integer> recruitmentIds = acceptedApplications.stream()
            .map(TeamRecruitmentApplication::getRecruitment)
            .map(TeamRecruitment::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        List<Integer> applicationIds = acceptedApplications.stream()
            .map(TeamRecruitmentApplication::getId)
            .filter(Objects::nonNull)
            .toList();

        Map<Integer, TeamRecruitmentChatRoom> teamRoomsByRecruitmentId = chatRoomRepository
            .findAllByRecruitment_IdInAndRoomScopeKeyAndRoomType(recruitmentIds, TEAM_ROOM_SCOPE_KEY, TEAM)
            .stream()
            .collect(Collectors.toMap(
                room -> room.getRecruitment().getId(),
                Function.identity()
            ));
        Map<Integer, TeamRecruitmentChatRoom> directRoomsByApplicationId = chatRoomRepository
            .findAllByApplication_IdInAndRoomType(applicationIds, DIRECT)
            .stream()
            .collect(Collectors.toMap(
                room -> room.getApplication().getId(),
                Function.identity()
            ));
        return new AcceptedChatRooms(teamRoomsByRecruitmentId, directRoomsByApplicationId);
    }

    private ApplicantSummary toApplicantSummary(TeamRecruitmentApplication application) {
        ProfileSnapshot snapshot = readProfileSnapshot(application.getProfileSnapshot());
        return new ApplicantSummary(
            application.getId(),
            snapshot.nickname(),
            snapshot.department(),
            snapshot.studentYear(),
            toApplicationRole(application.getRole()),
            application.getStatus(),
            application.getStatus() == ACCEPTED
        );
    }

    private RecruitmentCard toRecruitmentCard(TeamRecruitment recruitment) {
        return new RecruitmentCard(
            recruitment.getId(),
            recruitment.getCategory(),
            recruitment.getTitle(),
            recruitment.getMeetingType(),
            recruitment.getActivityStartDate(),
            recruitment.getActivityEndDate(),
            recruitment.getDeadlineDate(),
            dDay(recruitment),
            effectiveStatus(recruitment),
            recruitment.getRecruitmentType(),
            recruitment.getCurrentParticipants(),
            recruitment.getMaxParticipants(),
            toRecruitmentRoles(recruitment)
        );
    }

    private ApplicantRecruitment toApplicantRecruitment(TeamRecruitment recruitment) {
        TeamRecruitmentChatRoom teamRoom = findTeamRoom(recruitment.getId()).orElseThrow(() ->
            new KoinIllegalStateException(
                "팀원 모집글에 TEAM 채팅방이 없습니다. recruitmentId: " + recruitment.getId()
            )
        );
        return new ApplicantRecruitment(
            recruitment.getId(),
            recruitment.getCategory(),
            recruitment.getTitle(),
            recruitment.getMeetingType(),
            recruitment.getActivityStartDate(),
            recruitment.getActivityEndDate(),
            recruitment.getDeadlineDate(),
            dDay(recruitment),
            effectiveStatus(recruitment),
            recruitment.getRecruitmentType(),
            recruitment.getCurrentParticipants(),
            recruitment.getMaxParticipants(),
            toRecruitmentRoles(recruitment),
            true,
            teamRoom.getId()
        );
    }

    private List<RecruitmentRole> toRecruitmentRoles(TeamRecruitment recruitment) {
        if (recruitment.getRoles() == null) {
            return List.of();
        }
        boolean recruitmentClosed = recruitment.getStatus() != RECRUITING || isPastDeadline(recruitment);
        return recruitment.getRoles().stream()
            .map(role -> new RecruitmentRole(
                role.getId(),
                role.getName(),
                role.getCurrentParticipants(),
                role.getMaxParticipants(),
                recruitmentClosed || role.isClosed()
            ))
            .toList();
    }

    private TeamRecruitmentStatus effectiveStatus(TeamRecruitment recruitment) {
        if (recruitment.getStatus() == RECRUITING && isPastDeadline(recruitment)) {
            return TeamRecruitmentStatus.CLOSED;
        }
        return recruitment.getStatus();
    }

    private ApplicationRole toApplicationRole(TeamRecruitmentRole role) {
        if (role == null) {
            return null;
        }
        return new ApplicationRole(role.getId(), role.getName());
    }

    private Optional<TeamRecruitmentChatRoom> findTeamRoom(Integer recruitmentId) {
        return chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(recruitmentId, TEAM_ROOM_SCOPE_KEY)
            .filter(room -> room.getRoomType() == TeamRecruitmentChatRoomType.TEAM);
    }

    private Integer dDay(TeamRecruitment recruitment) {
        if (recruitment.getStatus() != RECRUITING || recruitment.getDeadlineDate() == null) {
            return null;
        }
        LocalDate today = LocalDate.now(clock.withZone(KST));
        if (today.isAfter(recruitment.getDeadlineDate())) {
            return null;
        }
        return Math.toIntExact(ChronoUnit.DAYS.between(today, recruitment.getDeadlineDate()));
    }

    private boolean isPastDeadline(TeamRecruitment recruitment) {
        return recruitment.getDeadlineDate() != null
            && LocalDate.now(clock.withZone(KST)).isAfter(recruitment.getDeadlineDate());
    }

    private ProfileSnapshot readProfileSnapshot(String profileSnapshot) {
        if (profileSnapshot == null || profileSnapshot.isBlank()) {
            throw new IllegalStateException("팀 모집 지원 프로필 snapshot이 비어 있습니다.");
        }
        try {
            return objectMapper.readValue(profileSnapshot, ProfileSnapshot.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("팀 모집 지원 프로필 snapshot을 읽을 수 없습니다.", exception);
        }
    }

    private record AcceptedChatRooms(
        Map<Integer, TeamRecruitmentChatRoom> teamRoomsByRecruitmentId,
        Map<Integer, TeamRecruitmentChatRoom> directRoomsByApplicationId
    ) {
    }
}
