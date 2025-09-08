package in.koreatech.koin.domain.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.dto.OrderPreparingResponse;
import in.koreatech.koin.domain.order.service.OrderPreparingQueryService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderPreparingController implements OrderPreparingApi {

    private final OrderPreparingQueryService service;

    @GetMapping("/in-progress")
    public ResponseEntity<List<OrderPreparingResponse>> getInprogressOrders(
        @Auth(permit = STUDENT) Integer userId
    ) {
        return ResponseEntity.ok(service.getInprogressOrders(userId));
    }
}
