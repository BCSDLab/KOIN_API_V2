package in.koreatech.koin.domain.shop.model.shop;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "shop_parent_categories")
public class ShopParentCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_message_id", referencedColumnName = "id", nullable = false)
    private ShopNotificationMessage notificationMessage;

    @Builder
    private ShopParentCategory(Integer id, String name, ShopNotificationMessage notificationMessage) {
        this.id = id;
        this.name = name;
        this.notificationMessage = notificationMessage;
    }
}
