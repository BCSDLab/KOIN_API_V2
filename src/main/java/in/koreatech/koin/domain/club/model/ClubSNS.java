package in.koreatech.koin.domain.club.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.club.enums.SNSType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "club_sns")
@NoArgsConstructor(access = PROTECTED)
public class ClubSNS {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    private Club club;

    @NotNull
    @Enumerated(value = STRING)
    @Column(name = "sns_type", nullable = false)
    private SNSType snsType;

    @NotNull
    @Size(max = 255)
    @Column(name = "contact", length = 255, nullable = false)
    private String contact;

    @Builder
    private ClubSNS(
        Integer id,
        Club club,
        SNSType snsType,
        String contact
    ) {
        this.id = id;
        this.club = club;
        this.snsType = snsType;
        this.contact = contact;
    }

    public ClubSNS(Club club, SNSType snsType, String contact) {
        this.club = club;
        this.snsType = snsType;
        this.contact = contact;
    }
}
