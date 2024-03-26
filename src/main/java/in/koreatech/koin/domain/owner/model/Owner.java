package in.koreatech.koin.domain.owner.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "owners")
public class Owner {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long id;

    @MapsId
    @OneToOne(cascade = ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Size(max = 12)
    @NotNull
    @Column(name = "company_registration_number", nullable = false, length = 12)
    private String companyRegistrationNumber;

    @Column(name = "grant_shop")
    private Boolean grantShop;

    @Column(name = "grant_event")
    private Boolean grantEvent;

    @OneToMany(cascade = {PERSIST, MERGE, REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "owner_id")
    private List<OwnerAttachment> attachments = new ArrayList<>();

    @Builder
    private Owner(
        User user,
        String companyRegistrationNumber,
        List<OwnerAttachment> attachments,
        Boolean grantShop,
        Boolean grantEvent
    ) {
        this.user = user;
        this.companyRegistrationNumber = companyRegistrationNumber;
        this.attachments = attachments;
        this.grantShop = grantShop;
        this.grantEvent = grantEvent;
    }
}
