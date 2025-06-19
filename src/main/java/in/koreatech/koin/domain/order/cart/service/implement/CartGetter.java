package in.koreatech.koin.domain.order.cart.service.implement;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartGetter {

    private final CartRepository cartRepository;

    public Optional<Cart> get(Integer userId) {
        return cartRepository.findCartByUserId(userId);
    }
}
