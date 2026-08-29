package in.koreatech.koin.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.APPLICANT_MANAGEMENT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.CHAT_ROOM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.MY_APPLICATIONS;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.NEW_APPLICATION;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.RECRUITMENT_CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_DUPLICATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_FINALIZED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CAPACITY_FULL;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CLOSED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_PROFILE_REQUIRED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ROLE_CLOSED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicationCreatedResponse;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicationRole;
import in.koreatech.koin.domain.team.recruitment.dto.CreateApplicationRequest;
import in.koreatech.koin.domain.team.recruitment.dto.ProfileActivity;
import in.koreatech.koin.domain.team.recruitment.dto.ProfileSnapshot;
import in.koreatech.koin.domain.team.recruitment.dto.UpdateApplicationStatusRequest;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileActivity;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileSkill;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMessageRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileActivityRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileSkillRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRoleRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.duplicate.DuplicateGuard;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentApplicationService {

    private static final String TEAM_ROOM_SCOPE_KEY = "TEAM";
    private static final String RECRUITMENT_CLOSED_REASON = "RECRUITMENT_CLOSED";
    private static final String OUTBOX_EVENT_TYPE = "TEAM_RECRUITMENT_NOTIFICATION";
    private static final String AGGREGATE_TYPE = "TEAM_RECRUITMENT";
    private static final String APPLICATION_UNIQUE_CONSTRAINT =
        "uk_team_recruitment_application_recruitment_applicant";
    private static final int SNAPSHOT_VERSION = 1;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TeamRecruitmentRepository recruitmentRepository;
    private final TeamRecruitmentRoleRepository roleRepository;
    private final TeamRecruitmentApplicationRepository applicationRepository;
    private final TeamRecruitmentProfileRepository profileRepository;
    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final TeamRecruitmentChatMemberRepository chatMemberRepository;
    private final TeamRecruitmentChatMessageRepository chatMessageRepository;
    private final TeamRecruitmentNotificationRepository notificationRepository;
    private final TeamRecruitmentOutboxEventRepository outboxEventRepository;
    private final TeamRecruitmentProfileActivityRepository profileActivityRepository;
    private final TeamRecruitmentProfileSkillRepository profileSkillRepository;
    private final StudentRepository studentRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;
    private final Clock clock;

    @Transactional
    @DuplicateGuard(
        key = "'team-recruitment:application:' + #recruitmentId + ':' + #studentId + ':' + #request.toString()",
        timeoutSeconds = 300
    )
    public ApplicationCreatedResponse createApplication(
        CreateApplicationRequest request,
        Integer recruitmentId,
        Integer studentId
    ) {
        if (request == null) {
            throw CustomException.of(INVALID_REQUEST_BODY);
        }

        TeamRecruitment recruitment = getLockedRecruitment(recruitmentId);
        validateRecruitmentCanReceiveApplication(recruitment, studentId);

        if (applicationRepository.findByRecruitmentIdAndApplicantIdWithLock(recruitmentId, studentId).isPresent()
            || applicationRepository.findByRecruitment_IdAndApplicant_Id(recruitmentId, studentId).isPresent()) {
            throw CustomException.of(TEAM_RECRUITMENT_APPLICATION_DUPLICATE);
        }

        Student student = studentRepository.getById(studentId);
        TeamRecruitmentProfile profile = profileRepository.findByUser_Id(studentId)
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_PROFILE_REQUIRED));
        List<TeamRecruitmentProfileSkill> skills = profileSkillRepository
            .findAllByProfile_UserIdOrderByDisplayOrderAsc(studentId);
        List<TeamRecruitmentProfileActivity> activities = profileActivityRepository
            .findAllByProfile_UserIdOrderByDisplayOrderAsc(studentId);

        TeamRecruitmentRole role = resolveApplicationRole(recruitment, request.roleId());
        validateCapacityForApplication(recruitment, role);

        TeamRecruitmentApplication application = TeamRecruitmentApplication.builder()
            .recruitment(recruitment)
            .applicant(student.getUser())
            .role(role)
            .motivation(request.motivation())
            .availability(request.availability())
            .status(PENDING)
            .profileSnapshot(writeProfileSnapshot(profile, student, skills, activities))
            .snapshotVersion(SNAPSHOT_VERSION)
            .build();

        TeamRecruitmentApplication savedApplication;
        try {
            savedApplication = applicationRepository.save(application);
            entityManager.flush();
        } catch (DataIntegrityViolationException exception) {
            if (isApplicationUniqueConstraintViolation(exception)) {
                throw CustomException.of(TEAM_RECRUITMENT_APPLICATION_DUPLICATE);
            }
            throw exception;
        }
        if (savedApplication == null) {
            savedApplication = application;
        }
        saveDecisionNotification(savedApplication, recruitment, NEW_APPLICATION, null);

        return new ApplicationCreatedResponse(
            savedApplication.getId(),
            recruitment.getId(),
            savedApplication.getStatus(),
            toApplicationRole(role),
            savedApplication.getCreatedAt()
        );
    }

    private boolean isApplicationUniqueConstraintViolation(DataIntegrityViolationException exception) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable cause = exception; cause != null && visited.add(cause); cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException constraintViolation
                && containsApplicationConstraintName(constraintViolation.getConstraintName())) {
                return true;
            }
            if (containsApplicationConstraintName(cause.getMessage())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsApplicationConstraintName(String value) {
        return value != null
            && value.toLowerCase(Locale.ROOT)
                .contains(APPLICATION_UNIQUE_CONSTRAINT.toLowerCase(Locale.ROOT));
    }

    @Transactional
    public void updateApplicationStatus(
        UpdateApplicationStatusRequest request,
        Integer recruitmentId,
        Integer applicationId,
        Integer authorId
    ) {
        if (request == null || request.status() == null || request.status() == PENDING) {
            throw CustomException.of(INVALID_REQUEST_BODY);
        }

        TeamRecruitment recruitment = getLockedRecruitment(recruitmentId);
        validateAuthor(recruitment, authorId);
        validateRecruitmentCanReceiveDecision(recruitment);

        TeamRecruitmentApplication application = applicationRepository
            .findByIdAndRecruitmentIdWithLock(applicationId, recruitmentId)
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_FOUND));
        validateApplicationBelongsToRecruitment(application, recruitmentId);
        if (application.getStatus() != PENDING) {
            throw CustomException.of(TEAM_RECRUITMENT_APPLICATION_FINALIZED);
        }

        TeamRecruitmentRole role = lockApplicationRole(application, recruitmentId);
        if (request.status() == REJECTED) {
            lockExistingTeamRoom(recruitment);
            application.reject("APPLICATION_REJECTED");
            applicationRepository.save(application);
            saveDecisionNotification(application, recruitment, APPLICATION_REJECTED, null);
            return;
        }

        validateCapacityForApproval(recruitment, role);
        TeamRecruitmentChatRoom teamRoom = getOrCreateLockedTeamRoom(recruitment);
        addMemberIfAbsent(teamRoom, application.getApplicant());

        application.accept();
        recruitment.increaseCurrentParticipants();
        if (role != null) {
            role.increaseCurrentParticipants();
            roleRepository.save(role);
        }
        applicationRepository.save(application);
        chatRoomRepository.save(teamRoom);
        entityManager.flush();

        saveDecisionNotification(application, recruitment, APPLICATION_ACCEPTED, teamRoom);

        if (isAtCapacity(recruitment)) {
            recruitment.close();
            entityManager.flush();
            notifyAcceptedMembersRecruitmentClosed(recruitment, teamRoom);
            rejectRemainingPendingApplications(recruitment);
        }
    }

    private TeamRecruitment getLockedRecruitment(Integer recruitmentId) {
        if (recruitmentId == null) {
            throw CustomException.of(TEAM_RECRUITMENT_NOT_FOUND);
        }
        return recruitmentRepository.findByIdWithLock(recruitmentId)
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_NOT_FOUND));
    }

    private void validateRecruitmentCanReceiveApplication(TeamRecruitment recruitment, Integer studentId) {
        if (studentId == null || (recruitment.getAuthor() != null
            && Objects.equals(recruitment.getAuthor().getId(), studentId))) {
            throw CustomException.of(TEAM_RECRUITMENT_FORBIDDEN);
        }
        if (recruitment.isDeleted()) {
            throw CustomException.of(TEAM_RECRUITMENT_NOT_FOUND);
        }
        if (recruitment.getStatus() != RECRUITING) {
            throw CustomException.of(TEAM_RECRUITMENT_CLOSED);
        }
        if (isPastDeadline(recruitment)) {
            throw CustomException.of(TEAM_RECRUITMENT_CLOSED);
        }
    }

    private void validateRecruitmentCanReceiveDecision(TeamRecruitment recruitment) {
        if (recruitment.isDeleted()) {
            throw CustomException.of(TEAM_RECRUITMENT_NOT_FOUND);
        }
        if (recruitment.getStatus() != RECRUITING || isPastDeadline(recruitment)) {
            throw CustomException.of(TEAM_RECRUITMENT_CLOSED);
        }
    }

    private void validateAuthor(TeamRecruitment recruitment, Integer userId) {
        if (userId == null || recruitment.getAuthor() == null
            || !Objects.equals(recruitment.getAuthor().getId(), userId)) {
            throw CustomException.of(TEAM_RECRUITMENT_FORBIDDEN);
        }
    }

    private TeamRecruitmentRole resolveApplicationRole(TeamRecruitment recruitment, Integer roleId) {
        if (recruitment.getRecruitmentType() == GENERAL) {
            if (roleId != null) {
                throw CustomException.of(INVALID_REQUEST_BODY);
            }
            return null;
        }
        if (roleId == null) {
            throw CustomException.of(INVALID_REQUEST_BODY);
        }

        TeamRecruitmentRole role = roleRepository.findByIdAndRecruitmentIdWithLock(roleId, recruitment.getId())
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_ROLE_CLOSED));
        validateRoleBelongsToRecruitment(role, recruitment.getId());
        if (role.isClosed()) {
            throw CustomException.of(TEAM_RECRUITMENT_ROLE_CLOSED);
        }
        return role;
    }

    private TeamRecruitmentRole lockApplicationRole(
        TeamRecruitmentApplication application,
        Integer recruitmentId
    ) {
        TeamRecruitmentRole role = application.getRole();
        if (role == null) {
            return null;
        }
        TeamRecruitmentRole lockedRole = roleRepository
            .findByIdAndRecruitmentIdWithLock(role.getId(), recruitmentId)
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_ROLE_CLOSED));
        validateRoleBelongsToRecruitment(lockedRole, recruitmentId);
        return lockedRole;
    }

    private void validateRoleBelongsToRecruitment(TeamRecruitmentRole role, Integer recruitmentId) {
        if (role.getRecruitment() == null || !Objects.equals(role.getRecruitment().getId(), recruitmentId)) {
            throw CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_FOUND);
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

    private void validateCapacityForApplication(TeamRecruitment recruitment, TeamRecruitmentRole role) {
        if (isAtCapacity(recruitment)) {
            throw CustomException.of(TEAM_RECRUITMENT_CAPACITY_FULL);
        }
        if (role != null && role.isClosed()) {
            throw CustomException.of(TEAM_RECRUITMENT_ROLE_CLOSED);
        }
    }

    private void validateCapacityForApproval(TeamRecruitment recruitment, TeamRecruitmentRole role) {
        if (isAtCapacity(recruitment)) {
            throw CustomException.of(TEAM_RECRUITMENT_CAPACITY_FULL);
        }
        if (recruitment.getRecruitmentType() != GENERAL && role == null) {
            throw CustomException.of(TEAM_RECRUITMENT_APPLICATION_NOT_FOUND);
        }
        if (role != null && role.isClosed()) {
            throw CustomException.of(TEAM_RECRUITMENT_ROLE_CLOSED);
        }
    }

    private boolean isAtCapacity(TeamRecruitment recruitment) {
        Integer current = recruitment.getCurrentParticipants();
        Integer maximum = recruitment.getMaxParticipants();
        return current != null && maximum != null && current >= maximum;
    }

    private boolean isPastDeadline(TeamRecruitment recruitment) {
        LocalDate deadline = recruitment.getDeadlineDate();
        return deadline != null && LocalDate.now(clock.withZone(KST)).isAfter(deadline);
    }

    private TeamRecruitmentChatRoom getOrCreateLockedTeamRoom(TeamRecruitment recruitment) {
        Optional<TeamRecruitmentChatRoom> existing = lockExistingTeamRoom(recruitment);
        if (existing.isPresent()) {
            return existing.get();
        }

        TeamRecruitmentChatRoom created = TeamRecruitmentChatRoom.builder()
            .recruitment(recruitment)
            .roomScopeKey(TEAM_ROOM_SCOPE_KEY)
            .roomType(TEAM)
            .status(ACTIVE)
            .build();
        TeamRecruitmentChatRoom saved = chatRoomRepository.save(created);
        TeamRecruitmentChatRoom room = saved == null ? created : saved;
        addMemberIfAbsent(room, recruitment.getAuthor());
        return room;
    }

    private Optional<TeamRecruitmentChatRoom> lockExistingTeamRoom(TeamRecruitment recruitment) {
        return chatRoomRepository
            .findByRecruitmentIdAndRoomScopeKeyWithLock(recruitment.getId(), TEAM_ROOM_SCOPE_KEY)
            .filter(room -> room.getRoomType() == TEAM);
    }

    private void addMemberIfAbsent(TeamRecruitmentChatRoom room, in.koreatech.koin.domain.user.model.User user) {
        if (room.getId() == null || user == null || user.getId() == null) {
            if (user != null) {
                TeamRecruitmentChatMember member = TeamRecruitmentChatMember.builder()
                    .chatRoom(room)
                    .user(user)
                    .build();
                room.addMember(member);
                chatMemberRepository.save(member);
            }
            return;
        }
        if (chatMemberRepository.existsByChatRoom_IdAndUser_Id(room.getId(), user.getId())) {
            return;
        }
        TeamRecruitmentChatMember member = TeamRecruitmentChatMember.builder()
            .chatRoom(room)
            .user(user)
            .lastReadMessageId(latestMessageId(room))
            .build();
        room.addMember(member);
        chatMemberRepository.save(member);
    }

    private Integer latestMessageId(TeamRecruitmentChatRoom room) {
        if (room.getId() == null) {
            return null;
        }
        return chatMessageRepository.findTopByChatRoom_IdOrderByIdDesc(room.getId())
            .map(in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage::getId)
            .orElse(null);
    }

    private void rejectRemainingPendingApplications(TeamRecruitment recruitment) {
        List<TeamRecruitmentApplication> pendingApplications = findApplicationsByStatus(recruitment, PENDING);
        for (TeamRecruitmentApplication pendingApplication : pendingApplications) {
            pendingApplication.reject(RECRUITMENT_CLOSED_REASON);
            applicationRepository.save(pendingApplication);
            saveDecisionNotification(pendingApplication, recruitment, APPLICATION_REJECTED, null);
        }
    }

    private List<TeamRecruitmentApplication> findApplicationsByStatus(
        TeamRecruitment recruitment,
        TeamRecruitmentApplicationStatus status
    ) {
        var page = applicationRepository.findAllByRecruitment_IdAndStatusIn(
            recruitment.getId(),
            List.of(status),
            org.springframework.data.domain.Pageable.unpaged()
        );
        return page == null ? List.of() : page.getContent();
    }

    private void notifyAcceptedMembersRecruitmentClosed(
        TeamRecruitment recruitment,
        TeamRecruitmentChatRoom teamRoom
    ) {
        List<TeamRecruitmentApplication> acceptedApplications = findApplicationsByStatus(recruitment, ACCEPTED);
        if (teamRoom == null && !acceptedApplications.isEmpty()) {
            throw new IllegalStateException(
                "승인된 지원자가 존재하지만 팀 모집 TEAM 채팅방이 없습니다. recruitmentId: "
                    + recruitment.getId()
            );
        }
        for (TeamRecruitmentApplication acceptedApplication : acceptedApplications) {
            saveDecisionNotification(acceptedApplication, recruitment, RECRUITMENT_CLOSED, teamRoom);
        }
    }

    private void saveDecisionNotification(
        TeamRecruitmentApplication application,
        TeamRecruitment recruitment,
        TeamRecruitmentNotificationType type,
        TeamRecruitmentChatRoom room
    ) {
        TeamRecruitmentNotificationTargetType targetType = type == APPLICATION_ACCEPTED
            || type == RECRUITMENT_CLOSED
            ? CHAT_ROOM
            : type == NEW_APPLICATION ? APPLICANT_MANAGEMENT : MY_APPLICATIONS;
        Integer applicationId = application.getId();
        String eventKey = "team-recruitment:application:" + applicationId + ":" + type.name();
        Optional<TeamRecruitmentOutboxEvent> existing = outboxEventRepository.findByEventKey(eventKey);
        if (existing != null && existing.isPresent()) {
            return;
        }

        TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
            .recipient(type == NEW_APPLICATION ? recruitment.getAuthor() : application.getApplicant())
            .type(type)
            .targetType(targetType)
            .messagePreview(notificationMessage(recruitment, type))
            .recruitment(recruitment)
            .application(application)
            .chatRoom(room)
            .build();
        TeamRecruitmentNotification savedNotification = notificationRepository.save(notification);
        if (savedNotification == null) {
            savedNotification = notification;
        }
        outboxEventRepository.save(TeamRecruitmentOutboxEvent.builder()
            .eventKey(eventKey)
            .eventType(OUTBOX_EVENT_TYPE)
            .aggregateType(AGGREGATE_TYPE)
            .aggregateId(recruitment.getId())
            .payload(notificationPayload(
                type == NEW_APPLICATION ? recruitment.getAuthor() : application.getApplicant(),
                application,
                recruitment,
                type,
                targetType,
                room,
                savedNotification.getId()
            ))
            .status(TeamRecruitmentOutboxEventStatus.PENDING)
            .build());
    }

    private String notificationMessage(
        TeamRecruitment recruitment,
        TeamRecruitmentNotificationType type
    ) {
        String title = recruitment.getTitle() == null ? "팀원 모집" : recruitment.getTitle();
        String message = switch (type) {
            case NEW_APPLICATION -> title + "에 새로운 지원자가 있어요.";
            case APPLICATION_ACCEPTED -> title + " 지원이 승인되었어요.";
            case RECRUITMENT_CLOSED -> title + " 모집이 마감되었어요.";
            default -> title + " 지원이 거절되었어요.";
        };
        return message.length() <= 255 ? message : message.substring(0, 255);
    }

    private String notificationPayload(
        User recipient,
        TeamRecruitmentApplication application,
        TeamRecruitment recruitment,
        TeamRecruitmentNotificationType type,
        TeamRecruitmentNotificationTargetType targetType,
        TeamRecruitmentChatRoom room,
        Integer notificationId
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", type.name());
        payload.put("target_type", targetType.name());
        payload.put("recipient_id", recipient == null ? null : recipient.getId());
        payload.put("recruitment_id", recruitment.getId());
        payload.put("application_id", application.getId());
        payload.put("chat_room_id", room == null ? null : room.getId());
        payload.put("notification_id", notificationId);
        payload.put("message_preview", notificationMessage(recruitment, type));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("팀 모집 알림 outbox payload를 직렬화할 수 없습니다.", exception);
        }
    }

    private String writeProfileSnapshot(
        TeamRecruitmentProfile profile,
        Student student,
        List<TeamRecruitmentProfileSkill> skills,
        List<TeamRecruitmentProfileActivity> activities
    ) {
        Integer admissionYear = studentYear(student.getStudentNumber());
        if (student.getDepartment() == null || admissionYear == null) {
            throw CustomException.of(TEAM_RECRUITMENT_PROFILE_REQUIRED);
        }
        List<String> snapshotSkills = skills == null
            ? List.of()
            : skills.stream().map(TeamRecruitmentProfileSkill::getSkill).toList();
        List<ProfileActivity> snapshotActivities = activities == null
            ? List.of()
            : activities.stream()
                .map(this::toProfileActivity)
                .toList();
        ProfileSnapshot snapshot = new ProfileSnapshot(
            profile.getProfileNickname(),
            student.getDepartment().getName(),
            admissionYear,
            profile.getPreferredRole(),
            snapshotSkills,
            snapshotActivities,
            profile.getSelfIntroduction()
        );
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("팀 모집 지원 프로필 snapshot을 직렬화할 수 없습니다.", exception);
        }
    }

    private ProfileActivity toProfileActivity(TeamRecruitmentProfileActivity activity) {
        return new ProfileActivity(
            activity.getId(),
            activity.getTitle(),
            activity.getStartedAt(),
            activity.getEndedAt(),
            activity.getIsOngoing(),
            activity.getDescription()
        );
    }

    private Integer studentYear(String studentNumber) {
        if (studentNumber == null || studentNumber.length() < 4) {
            return null;
        }
        try {
            return Integer.valueOf(studentNumber.substring(0, 4));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private ApplicationRole toApplicationRole(TeamRecruitmentRole role) {
        if (role == null) {
            return null;
        }
        return new ApplicationRole(role.getId(), role.getName());
    }
}
