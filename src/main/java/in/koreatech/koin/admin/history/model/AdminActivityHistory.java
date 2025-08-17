package in.koreatech.koin.admin.history.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.admin.history.enums.DomainType;
import in.koreatech.koin.admin.history.enums.HttpMethodType;
import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "admins_activity_history")
@NoArgsConstructor(access = PROTECTED)
public class AdminActivityHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "domain_id")
    private Integer domainId;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    @Column(name = "request_method", nullable = false, length = 10)
    private HttpMethodType requestMethod;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    @Column(name = "domain_name", nullable = false, length = 20)
    private DomainType domainName;

    @Column(name = "request_message", columnDefinition = "TEXT")
    private String requestMessage;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Builder
    public AdminActivityHistory(
        Integer domainId,
        HttpMethodType requestMethod,
        DomainType domainName,
        String requestMessage,
        Admin admin
    ) {
        this.domainId = domainId;
        this.requestMethod = requestMethod;
        this.domainName = domainName;
        this.requestMessage = requestMessage;
        this.admin = admin;
    }
}
