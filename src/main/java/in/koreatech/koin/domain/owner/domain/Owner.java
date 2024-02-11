package in.koreatech.koin.domain.owner.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "owners")
public class Owner {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Size(max = 12)
    @NotNull
    @Column(name = "company_registration_number", nullable = false, length = 12)
    private String companyRegistrationNumber;

    @Size(max = 255)
    @Column(name = "company_registration_certificate_image_url")
    private String companyRegistrationCertificateImageUrl;

    @Column(name = "grant_shop")
    private Byte grantShop;

    @Column(name = "grant_event")
    private Byte grantEvent;
}
