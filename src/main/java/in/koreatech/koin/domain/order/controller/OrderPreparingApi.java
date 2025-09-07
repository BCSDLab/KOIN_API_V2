package in.koreatech.koin.domain.order.controller;

import static in.koreatech.koin.domain.user.model.UserType.GENERAL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.order.dto.OrderPreparingResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/order")
@Tag(name = "주문내역 페이지 - 주문 중", description = "활성화된 주문을 조회한다")
public interface OrderPreparingApi {
    @Operation(
        summary = "주문 내역 중 현재 활성화된 주문을 조회한다",
        description = """
            예상 시각,
            가게 이름,
            배달 or 포장,
            가게 썸네일,
            주문 내용
            """
    )
    @GetMapping
    ResponseEntity<List<OrderPreparingResponse>> listOrderPreparing(
        @Auth(permit = {GENERAL, STUDENT}) Integer userId
    );
}
