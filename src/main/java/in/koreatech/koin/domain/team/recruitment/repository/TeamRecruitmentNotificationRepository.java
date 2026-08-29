package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamRecruitmentNotificationRepository extends Repository<TeamRecruitmentNotification, Integer> {

    TeamRecruitmentNotification save(TeamRecruitmentNotification notification);

    Optional<TeamRecruitmentNotification> findById(Integer id);

    Page<TeamRecruitmentNotification> findAllByRecipient_IdOrderByIdDesc(Integer recipientId, Pageable pageable);

    Page<TeamRecruitmentNotification> findAllByRecipient_IdAndIsDeletedFalseOrderByIdDesc(
        Integer recipientId, Pageable pageable
    );

    long countByRecipient_IdAndReadAtIsNullAndIsDeletedFalse(Integer recipientId);

    Optional<TeamRecruitmentNotification> findByIdAndRecipient_Id(Integer id, Integer recipientId);

    List<TeamRecruitmentNotification> findAllByRecipient_IdAndIsDeletedFalse(Integer recipientId);

    Page<TeamRecruitmentNotification> findAllByRecipient_IdAndTypeInOrderByIdDesc(
        Integer recipientId,
        Collection<TeamRecruitmentNotificationType> types,
        Pageable pageable
    );

    @Query("""
        SELECT notification
        FROM TeamRecruitmentNotification notification
        JOIN FETCH notification.recipient recipient
        JOIN FETCH notification.recruitment recruitment
        LEFT JOIN FETCH notification.application application
        LEFT JOIN FETCH notification.chatRoom chatRoom
        WHERE notification.id = :notificationId
        """)
    Optional<TeamRecruitmentNotification> findByIdForOutbox(
        @Param("notificationId") Integer notificationId
    );

    @Query("""
        SELECT notification
        FROM TeamRecruitmentNotification notification
        JOIN FETCH notification.recipient recipient
        JOIN FETCH notification.recruitment recruitment
        LEFT JOIN FETCH notification.application application
        LEFT JOIN FETCH notification.chatRoom chatRoom
        WHERE recipient.id = :recipientId
          AND notification.type = :type
          AND notification.targetType = :targetType
          AND recruitment.id = :recruitmentId
          AND ((:applicationId IS NULL AND application IS NULL) OR application.id = :applicationId)
          AND ((:chatRoomId IS NULL AND chatRoom IS NULL) OR chatRoom.id = :chatRoomId)
        ORDER BY notification.id DESC
        """)
    List<TeamRecruitmentNotification> findForOutbox(
        @Param("recipientId") Integer recipientId,
        @Param("type") TeamRecruitmentNotificationType type,
        @Param("targetType") TeamRecruitmentNotificationTargetType targetType,
        @Param("recruitmentId") Integer recruitmentId,
        @Param("applicationId") Integer applicationId,
        @Param("chatRoomId") Integer chatRoomId
    );
}
