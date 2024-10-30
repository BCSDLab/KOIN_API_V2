package in.koreatech.koin.admin.history.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "admins_activity_history")
@NoArgsConstructor(access = PROTECTED)
public class AdminActivityHistory {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "domain_id")
    private Integer domainId;

    @NotNull
    @Size(max = 10)
    @Column(name = "request_method", nullable = false, length = 10)
    private String requestMethod;

    @NotNull
    @Size(max = 20)
    @Column(name = "domain_name", nullable = false, length = 20)
    private String domainName;

    @Column(name = "request_message", columnDefinition = "TEXT")
    private String requestMessage;

    @NotNull
    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /* TODO. admin PR 머지 되면 admin으로 바꾸기 */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public AdminActivityHistory(Integer domainId, String requestMethod, String domainName, String requestMessage,
        LocalDateTime createdAt, User user) {
        this.domainId = domainId;
        this.requestMethod = requestMethod;
        this.domainName = domainName;
        this.requestMessage = requestMessage;
        this.createdAt = createdAt;
        this.user = user;
    }
}
