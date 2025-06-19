package in.koreatech.koin.domain.order.cart.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGetter {

    private final UserRepository userRepository;

    public User get(Integer userId) {
        return userRepository.getById(userId);
    }
}
