package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "device", schema = "koin")
public class Device extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(mappedBy = "device", cascade = CascadeType.PERSIST)
    private AccessHistory accessHistory;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 100)
    @Column(name = "model", length = 100)
    private String model;

    @Size(max = 100)
    @Column(name = "type", length = 100)
    private String type;

    @Builder
    private Device(
        Integer id,
        AccessHistory accessHistory,
        User user,
        String model,
        String type
    ) {
        this.id = id;
        this.accessHistory = accessHistory;
        this.user = user;
        this.model = model;
        this.type = type;
    }

    public void setAccessHistory(AccessHistory accessHistory) {
        this.accessHistory = accessHistory;
    }

    public void changeUser(User user) {
        this.user = user;
    }

    public void setModelInfo(String model, String type) {
        this.model = model;
        this.type = type;
    }
}
