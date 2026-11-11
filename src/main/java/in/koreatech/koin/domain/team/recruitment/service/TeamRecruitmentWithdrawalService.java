package in.koreatech.koin.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.chat.event.ChatRoomChangedEvent;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRoleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamRecruitmentWithdrawalService {

    private final TeamRecruitmentRepository recruitmentRepository;
    private final TeamRecruitmentChatRoomRepository chatRoomRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TeamRecruitmentApplicationRepository applicationRepository;
    private final TeamRecruitmentRoleRepository roleRepository;
    private final TeamRecruitmentClosureService closureService;
    private final TeamRecruitmentOutboxEventRepository outboxEventRepository;

    @Transactional
    public void prepareWithdrawal(Integer userId) {
        Set<Integer> recruitmentIds = new TreeSet<>(recruitmentRepository.findIdsByAuthorId(userId));
        recruitmentIds.addAll(applicationRepository.findRecruitmentIdsByApplicantId(userId));
        for (Integer recruitmentId : recruitmentIds) {
            TeamRecruitment recruitment = recruitmentRepository.findByIdWithLock(recruitmentId).orElse(null);
            if (recruitment == null) {
                continue;
            }
            TeamRecruitmentApplication application = applicationRepository
                .findByRecruitmentIdAndApplicantIdWithLock(recruitment.getId(), userId)
                .orElse(null);
            if (application != null) {
                withdrawApplication(recruitment, application);
            }

            boolean isAuthor = recruitment.getAuthor() != null
                && Objects.equals(recruitment.getAuthor().getId(), userId);
            if (isAuthor || application != null) {
                chatRoomRepository.findAllByRecruitment_Id(recruitmentId).forEach(room ->
                    eventPublisher.publishEvent(new ChatRoomChangedEvent(
                        new ChatRoomKey(ChatDomainType.TEAM_RECRUITMENT, recruitmentId, room.getId()))));
            }
            if (isAuthor && recruitment.isRecruiting()) {
                recruitment.close();
                closureService.onAuthorWithdrawn(recruitment);
            }
        }
        outboxEventRepository.findUnpublishedNotificationIdsByRecipientId(userId)
            .forEach(outboxEventRepository::deleteUnpublishedById);
    }

    private void withdrawApplication(TeamRecruitment recruitment, TeamRecruitmentApplication application) {
        if (application.getStatus() == PENDING) {
            application.cancel("USER_WITHDRAWN");
        } else if (application.getStatus() == ACCEPTED) {
            recruitment.decreaseCurrentParticipants();
            if (application.getRole() != null) {
                roleRepository.findByIdAndRecruitmentIdWithLock(application.getRole().getId(), recruitment.getId())
                    .orElseThrow().decreaseCurrentParticipants();
            }
        }
    }
}
