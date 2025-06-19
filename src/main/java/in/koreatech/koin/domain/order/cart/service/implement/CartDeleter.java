package in.koreatech.koin.domain.order.cart.service.implement;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartDeleter {

    private final CartRepository cartRepository;

    public void deleteByUserId(Integer userId) {
        cartRepository.deleteByUserId(userId);
    }
}
