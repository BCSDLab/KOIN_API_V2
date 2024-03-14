package in.koreatech.koin.domain.owner.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
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
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Size(max = 12)
    @NotNull
    @Column(name = "company_registration_number", nullable = false, length = 12)
    private String companyRegistrationNumber;

    @Size(max = 255)
    @Column(name = "company_registration_certificate_image_url")
    private String companyRegistrationCertificateImageUrl;

    @Column(name = "grant_shop")
    private Boolean grantShop;

    @Column(name = "grant_event")
    private Boolean grantEvent;

    @Builder
    private Owner(User user, String companyRegistrationNumber,
        String companyRegistrationCertificateImageUrl, Boolean grantShop, Boolean grantEvent) {
        this.user = user;
        this.companyRegistrationNumber = companyRegistrationNumber;
        this.companyRegistrationCertificateImageUrl = companyRegistrationCertificateImageUrl;
        this.grantShop = grantShop;
        this.grantEvent = grantEvent;
    }
}
