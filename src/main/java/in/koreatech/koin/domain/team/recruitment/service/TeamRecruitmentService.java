package in.koreatech.koin.domain.team.recruitment.service;

import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CLOSED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_MAX_PARTICIPANTS_BELOW_ACCEPTED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ROLE_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_TYPE_CHANGE_NOT_ALLOWED;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.team.recruitment.dto.CreateRecruitmentRequest;
import in.koreatech.koin.domain.team.recruitment.dto.IdResponse;
import in.koreatech.koin.domain.team.recruitment.dto.RoleInput;
import in.koreatech.koin.domain.team.recruitment.dto.UpdateRecruitmentRequest;
import in.koreatech.koin.domain.team.recruitment.dto.UpdateRoleInput;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.duplicate.DuplicateGuard;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentService {

    // display_order 는 CHECK 제약이 1 이상을 요구한다.
    private static final int FIRST_DISPLAY_ORDER = 1;

    // display_order 는 CHECK 제약이 1~5 만 허용한다.
    private static final int MAX_DISPLAY_ORDER = 5;

    // (recruitment_id, name) unique 제약을 피하기 위한 임시 이름. name 컬럼은 10자까지다.
    private static final String TEMPORARY_NAME_PREFIX = "#";
    private static final int MAX_ROLE_NAME_LENGTH = 10;

    // 예약 이름은 기존 역할 5개와 요청 이름 5개를 합쳐 최대 10개다.
    private static final int TEMPORARY_NAME_CANDIDATES = 32;

    private final TeamRecruitmentRepository teamRecruitmentRepository;
    private final TeamRecruitmentApplicationRepository applicationRepository;
    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final TeamRecruitmentChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;
    private final TeamRecruitmentClosureService closureService;
    private final EntityManager entityManager;
    private final Clock clock;

    /**
     * 모집글과 TEAM 채팅방, 작성자 멤버를 같은 트랜잭션에서 생성한다.
     */
    @Transactional
    @DuplicateGuard(
        key = "'team-recruitment:create:' + #userId + ':' + #request.toString()",
        timeoutSeconds = 300
    )
    public IdResponse createRecruitment(Integer userId, CreateRecruitmentRequest request) {
        User author = userRepository.getById(userId);
        TeamRecruitment recruitment = request.toEntity(author);
        addRoles(recruitment, request.roles());

        TeamRecruitment saved = teamRecruitmentRepository.save(recruitment);
        createTeamChatRoom(saved, author);
        return new IdResponse(saved.getId());
    }

    @Transactional
    public void updateRecruitment(Integer userId, Integer recruitmentId, UpdateRecruitmentRequest request) {
        TeamRecruitment recruitment = getModifiableRecruitment(userId, recruitmentId);
        if (!recruitment.isRecruiting()) {
            throw CustomException.of(TEAM_RECRUITMENT_CLOSED);
        }

        validateTypeChange(recruitment, request);
        validateMaxParticipants(recruitment, request.resolveMaxParticipants());
        replaceRoles(recruitment, request.roles());
        recruitment.modify(
            request.category(),
            request.title(),
            request.meetingType(),
            request.activityStartDate(),
            request.activityEndDate(),
            request.deadlineDate(),
            request.recruitmentType(),
            request.resolveMaxParticipants(),
            request.description(),
            request.relatedUrl(),
            request.qualification()
        );
        closeIfCapacityFull(recruitment);
    }

    /**
     * 정원을 줄여 이미 승인된 인원과 같아지면 그 자리에서 마감된다.
     * 정원 충족 자동 마감이므로 TEAM 채팅방은 ACTIVE 를 유지한다.
     */
    private void closeIfCapacityFull(TeamRecruitment recruitment) {
        if (recruitment.getCurrentParticipants() < recruitment.getMaxParticipants()) {
            return;
        }
        recruitment.close();
        entityManager.flush();
        closureService.onCapacityFull(recruitment);
    }

    /**
     * soft delete 이며 이미 삭제된 모집글에 대한 재요청도 성공으로 처리한다.
     */
    @Transactional
    public void deleteRecruitment(Integer userId, Integer recruitmentId) {
        TeamRecruitment recruitment = getOwnedRecruitment(userId, recruitmentId);
        if (recruitment.isDeleted()) {
            return;
        }
        recruitment.markDeleted(LocalDateTime.now(clock));
        closureService.onDeleted(recruitment);
    }

    /**
     * 이미 마감된 모집글에 대한 재요청도 성공으로 처리한다.
     */
    @Transactional
    public void closeRecruitment(Integer userId, Integer recruitmentId) {
        TeamRecruitment recruitment = getModifiableRecruitment(userId, recruitmentId);
        if (!recruitment.isRecruiting()) {
            return;
        }
        recruitment.close();
        closureService.onClosed(recruitment);
    }

    /**
     * 지원자가 이미 있으면 모집 유형을 바꿀 수 없다.
     * GENERAL 지원서는 역할이 없고 ROLE_BASED 지원서는 역할이 필수여서 전환하면 기존 지원서가 규칙을 어긴다.
     */
    private void validateTypeChange(TeamRecruitment recruitment, UpdateRecruitmentRequest request) {
        if (recruitment.getRecruitmentType() == request.recruitmentType()) {
            return;
        }
        if (applicationCountOf(recruitment) > 0) {
            throw CustomException.of(TEAM_RECRUITMENT_TYPE_CHANGE_NOT_ALLOWED,
                "recruitmentId: " + recruitment.getId());
        }
    }

    /**
     * 승인된 인원보다 적은 정원으로는 수정할 수 없다.
     */
    private void validateMaxParticipants(TeamRecruitment recruitment, int maxParticipants) {
        if (maxParticipants < recruitment.getCurrentParticipants()) {
            throw CustomException.of(TEAM_RECRUITMENT_MAX_PARTICIPANTS_BELOW_ACCEPTED,
                "recruitmentId: " + recruitment.getId());
        }
    }

    /**
     * 거절된 지원서도 모집글에 남아 role_id 규칙을 어기게 되므로 모든 상태를 센다.
     */
    private long applicationCountOf(TeamRecruitment recruitment) {
        return applicationRepository.countByRecruitment_IdAndStatusIn(
            recruitment.getId(), List.of(TeamRecruitmentApplicationStatus.values()));
    }

    private TeamRecruitment getOwnedRecruitment(Integer userId, Integer recruitmentId) {
        TeamRecruitment recruitment = teamRecruitmentRepository.findByIdWithLock(recruitmentId)
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_NOT_FOUND, "recruitmentId: " + recruitmentId));
        if (!recruitment.getAuthor().getId().equals(userId)) {
            throw CustomException.of(TEAM_RECRUITMENT_FORBIDDEN, "recruitmentId: " + recruitmentId);
        }
        return recruitment;
    }

    private TeamRecruitment getModifiableRecruitment(Integer userId, Integer recruitmentId) {
        TeamRecruitment recruitment = getOwnedRecruitment(userId, recruitmentId);
        if (recruitment.isDeleted()) {
            throw CustomException.of(TEAM_RECRUITMENT_NOT_FOUND, "recruitmentId: " + recruitmentId);
        }
        return recruitment;
    }

    private void addRoles(TeamRecruitment recruitment, List<RoleInput> roles) {
        IntStream.range(0, roles.size())
            .forEach(index -> recruitment.addRole(TeamRecruitmentRole.builder()
                .name(roles.get(index).name())
                .maxParticipants(roles.get(index).maxParticipants())
                .currentParticipants(0)
                .displayOrder(index + FIRST_DISPLAY_ORDER)
                .build()));
    }

    /**
     * 지원자가 있는 역할은 삭제, 이름 변경, 정원 축소를 할 수 없다.
     * 기존 역할은 id 로 갱신하고 id 가 없는 항목은 새 역할로 추가한다.
     * <p>
     * display_order 에는 (recruitment_id, display_order) unique 제약과 BETWEEN 1 AND 5 CHECK 가 함께 걸려 있다.
     * 슬롯이 1~5 로 한정되어 있어 기존 역할의 순서를 서로 맞바꿀 임시 자리가 없으므로,
     * 기존 역할은 자신의 순서를 유지하고 새 역할만 비어 있는 슬롯을 채운다.
     * 삭제로 비는 슬롯을 새 역할이 쓸 수 있도록 삭제를 먼저 flush 한다.
     * <p>
     * 이름에도 (recruitment_id, name) unique 제약이 있어 두 역할의 이름을 서로 맞바꾸면
     * 중간 update 에서 충돌한다. 그래서 이름이 바뀌는 역할을 먼저 임시 이름으로 옮기고
     * flush 한 뒤 최종 이름을 부여한다.
     */
    private void replaceRoles(TeamRecruitment recruitment, List<UpdateRoleInput> requestedRoles) {
        Map<Integer, TeamRecruitmentRole> existing = recruitment.getRoles().stream()
            .collect(Collectors.toMap(TeamRecruitmentRole::getId, Function.identity(), (a, b) -> a, HashMap::new));

        List<TeamRecruitmentRole> keptRoles = new ArrayList<>();
        List<UpdateRoleInput> newRoles = new ArrayList<>();
        Map<TeamRecruitmentRole, UpdateRoleInput> renamed = new LinkedHashMap<>();
        for (UpdateRoleInput requested : requestedRoles) {
            if (requested.isNew()) {
                newRoles.add(requested);
                continue;
            }
            TeamRecruitmentRole role = existing.remove(requested.id());
            if (role == null) {
                throw CustomException.of(TEAM_RECRUITMENT_ROLE_NOT_FOUND, "roleId: " + requested.id());
            }
            validateRoleModifiable(role, requested);
            keptRoles.add(role);
            if (role.getName().equals(requested.name())) {
                role.modify(requested.name(), requested.maxParticipants(), role.getDisplayOrder());
            } else {
                renamed.put(role, requested);
            }
        }

        existing.values().forEach(this::validateRoleRemovable);
        existing.values().forEach(recruitment::removeRole);
        moveRenamedToTemporaryNames(renamed, recruitment, requestedRoles);
        entityManager.flush();

        renamed.forEach((role, requested) ->
            role.modify(requested.name(), requested.maxParticipants(), role.getDisplayOrder()));
        int displayOrder = firstOrderForNewRoles(keptRoles, newRoles.size());
        for (UpdateRoleInput requested : newRoles) {
            recruitment.addRole(TeamRecruitmentRole.builder()
                .name(requested.name())
                .maxParticipants(requested.maxParticipants())
                .currentParticipants(0)
                .displayOrder(displayOrder++)
                .build());
        }
    }

    /**
     * 이름이 바뀌는 역할을 다른 어떤 이름과도 겹치지 않는 임시 이름으로 옮긴다.
     */
    private void moveRenamedToTemporaryNames(
        Map<TeamRecruitmentRole, UpdateRoleInput> renamed,
        TeamRecruitment recruitment,
        List<UpdateRoleInput> requestedRoles
    ) {
        if (renamed.isEmpty()) {
            return;
        }
        Set<String> reserved = new HashSet<>();
        recruitment.getRoles().forEach(role -> reserved.add(role.getName()));
        requestedRoles.forEach(requested -> reserved.add(requested.name()));
        for (TeamRecruitmentRole role : renamed.keySet()) {
            role.modify(temporaryName(reserved), role.getMaxParticipants(), role.getDisplayOrder());
        }
    }

    /**
     * 역할 이름에 쓸 수 있는 문자에 제한이 없으므로 "#0" 같은 이름도 사용자가 쓸 수 있다.
     * 예약된 이름은 기존 역할과 요청 이름을 합쳐 최대 10개라, 후보를 그보다 넉넉히 두면 항상 빈 자리가 있다.
     * 후보는 잘림이 일어나지 않는 길이로만 만든다.
     */
    private String temporaryName(Set<String> reserved) {
        for (int candidate = 0; candidate < TEMPORARY_NAME_CANDIDATES; candidate++) {
            String name = TEMPORARY_NAME_PREFIX + candidate;
            if (name.length() <= MAX_ROLE_NAME_LENGTH && reserved.add(name)) {
                return name;
            }
        }
        throw CustomException.of(TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION);
    }

    /**
     * 새 역할이 시작할 display_order 를 구한다.
     * display_order 는 1~5 만 허용하므로 기존 역할 뒤에 자리가 부족하면
     * 기존 역할을 1..n 으로 압축해 뒤쪽 자리를 만든다.
     * 요청 단계에서 역할 개수를 5개로 제한하므로 압축 후에는 항상 자리가 남는다.
     */
    private int firstOrderForNewRoles(List<TeamRecruitmentRole> keptRoles, int newRoleCount) {
        List<TeamRecruitmentRole> sorted = keptRoles.stream()
            .sorted(Comparator.comparingInt(TeamRecruitmentRole::getDisplayOrder))
            .toList();
        int highest = sorted.isEmpty() ? 0 : sorted.get(sorted.size() - 1).getDisplayOrder();
        if (highest + newRoleCount <= MAX_DISPLAY_ORDER) {
            return highest + 1;
        }
        compactDisplayOrders(sorted);
        return sorted.size() + FIRST_DISPLAY_ORDER;
    }

    /**
     * 기존 역할의 상대 순서를 유지한 채 display_order 를 1..n 으로 당긴다.
     * (recruitment_id, display_order) unique 때문에 낮은 순서부터 옮기고 단계마다 flush 한다.
     * 오름차순으로 옮기면 목표 자리는 비어 있거나 옮길 필요가 없는 자리다.
     */
    private void compactDisplayOrders(List<TeamRecruitmentRole> sortedByDisplayOrder) {
        int order = FIRST_DISPLAY_ORDER;
        for (TeamRecruitmentRole role : sortedByDisplayOrder) {
            if (role.getDisplayOrder() != order) {
                role.modify(role.getName(), role.getMaxParticipants(), order);
                entityManager.flush();
            }
            order++;
        }
    }

    private void validateRoleModifiable(TeamRecruitmentRole role, UpdateRoleInput requested) {
        boolean renamed = !role.getName().equals(requested.name());
        boolean capacityReduced = requested.maxParticipants() < role.getMaxParticipants();
        if ((renamed || capacityReduced) && hasApplicants(role)) {
            throw CustomException.of(TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED, "roleId: " + role.getId());
        }
    }

    private void validateRoleRemovable(TeamRecruitmentRole role) {
        if (hasApplicants(role)) {
            throw CustomException.of(TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED, "roleId: " + role.getId());
        }
    }

    /**
     * 거절된 지원서도 역할을 계속 참조하고 role_id FK 가 ON DELETE RESTRICT 이므로
     * 한 번이라도 지원서가 생긴 역할은 삭제, 이름 변경, 정원 축소를 막는다.
     */
    private boolean hasApplicants(TeamRecruitmentRole role) {
        return Arrays.stream(TeamRecruitmentApplicationStatus.values())
            .anyMatch(status -> applicationRepository.countByRole_IdAndStatus(role.getId(), status) > 0);
    }

    private void createTeamChatRoom(TeamRecruitment recruitment, User author) {
        TeamRecruitmentChatRoom room = chatRoomRepository.save(TeamRecruitmentChatRoom.createTeamRoom(recruitment));
        TeamRecruitmentChatMember member = TeamRecruitmentChatMember.builder()
            .chatRoom(room)
            .user(author)
            .build();
        room.addMember(member);
        chatMemberRepository.save(member);
    }
}
