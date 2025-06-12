package in.koreatech.koin.domain.order.shop.model.domain;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.CascadeType.REMOVE;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.domain.shop.model.menu.MenuOrigin;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Embeddable
@RequiredArgsConstructor
public class ShopMenuOrigins {

    @OneToMany(mappedBy = "shop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE})
    private List<MenuOrigin> origin = new ArrayList<>();

}
