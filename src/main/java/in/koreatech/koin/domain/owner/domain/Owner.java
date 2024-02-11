package in.koreatech.koin.domain.owner.domain;

import java.util.List;

import in.koreatech.koin.domain.shop.model.Shop;
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

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "owner")
    private List<Shop> shops;

    @OneToMany(mappedBy = "owner")
    private List<OwnerAttachment> attachments;

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
