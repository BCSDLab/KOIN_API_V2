package in.koreatech.koin.domain.order.delivery.controller;

import static in.koreatech.koin.domain.user.model.UserType.GENERAL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.code.ApiResponseCodes;
import in.koreatech.koin.domain.order.delivery.dto.RiderMessageResponse;
import in.koreatech.koin.domain.order.delivery.dto.UserCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.delivery.dto.UserDeliveryAddressResponse;
import in.koreatech.koin.domain.order.delivery.dto.UserOffCampusDeliveryAddressRequest;
import in.koreatech.koin.domain.order.delivery.dto.UserOffCampusDeliveryAddressValidateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Delivery: 배달", description = "배달 API")
public interface DeliveryApi {

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "교외 배달 주소 생성 및 검증 성공",
            content = @Content(mediaType = "application/json", examples =
            @ExampleObject(
                value = """
                        {
                          "user_delivery_address_id": "16",
                          "full_address": "충청남도 천안시 동남구 병천면 충절로 1628-17 에듀윌 301호",
                          "to_rider": "문 앞에 놓아주세요."
                        }
                        """
            )
            )
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "배달 불가 지역", summary = "배달 불가 지역", value = """
                    {
                      "code": "INVALID_DELIVERY_AREA",
                      "message": "배달이 불가능한 지역입니다.",
                      "errorTraceId": "a3e73b85-8c84-41f1-9f24-e6a4ae38aea3"
                    }
                    """),
                @ExampleObject(name = "전체 주소 누락", summary = "전체 주소 누락", value = """
                    {
                      "code": "",
                      "message": "전체 주소는 필수입니다.",
                      "errorTraceId": "f5f94346-99fb-4197-8a73-f5d8c44a3525"
                    }
                    """),
                @ExampleObject(name = "상세 주소 누락", summary = "상세 주소 누락", value = """
                    {
                      "code": "",
                      "message": "상세주소는 필수입니다.",
                      "errorTraceId": "f5f94346-99fb-4197-8a73-f5d8c44a3525"
                    }
                    """),
                @ExampleObject(name = "우편번호 누락", summary = "우편번호 누락", value = """
                    {
                      "code": "",
                      "message": "우편번호는 필수입니다.",
                      "errorTraceId": "f5f94346-99fb-4197-8a73-f5d8c44a3525"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "401", description = "인증 정보 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "인증 정보 오류", summary = "인증 정보 오류", value = """
                    {
                      "code": "",
                      "message": "올바르지 않은 인증정보입니다.",
                      "errorTraceId": "5ba40351-6d27-40e5-90e3-80c5cf08a1ac"
                    }
                    """)
            })
        )
    })
    @Operation(summary = "교외 배달 주소 추가", description = """
        ### 사용자의 교외 배달 주소를 새로 추가
        - 사용자가 입력한 주소 정보를 저장하고, 배달 가능 지역인지 검증합니다.
        - **full_address** 는 도로명 주소 + 사용자가 입력한 상세 주소를 합친 값을 입력 해야 합니다.
            - ex) 충청남도 천안시 동남구 병천면 충절로 1628-17 **에듀윌 301호**
        - **zip_number**, **detail_address**, **full_address**는 필수 항목
        - 주소가 **충청남도 천안시 동남구 병천면** 이 아닌 경우 예외
        """)
    @PostMapping("/delivery/address/off-campus")
    ResponseEntity<UserDeliveryAddressResponse> addOffCampusDeliveryAddress(
        @RequestBody @Valid UserOffCampusDeliveryAddressRequest request,
        @Parameter(hidden = true) @Auth(permit = {GENERAL, STUDENT}) Integer userId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "교내 배달 주소 생성 성공",
            content = @Content(mediaType = "application/json", examples =
            @ExampleObject(
                value = """
                        {
                          "user_delivery_address_id": 16,
                          "full_address": "충남 천안시 동남구 병천면 충절로 1600 한국기술교육대학교 제1캠퍼스 생활관 101동",
                          "to_rider": "전화주시면 마중 나갈게요"
                        }
                        """
            )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 정보 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "인증 정보 오류", summary = "인증 정보 오류", value = """
                    {
                      "code": "",
                      "message": "올바르지 않은 인증정보입니다.",
                      "errorTraceId": "5ba40351-6d27-40e5-90e3-80c5cf08a1ac"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 리소스",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "존재하지 않는 교내 배달 주소 ID", summary = "존재하지 않는 교내 배달 주소", value = """
                    {
                      "code": "NOT_FOUND_DELIVERY_ADDRESS",
                      "message": "교내 배달 주소를 찾을 수 없습니다.",
                      "errorTraceId": "5ba40351-6d27-40e5-90e3-80c5cf08a1ac"
                    }
                    """)
            })
        )
    })
    @Operation(summary = "교내 배달 주소 추가", description = """
        ### 사용자의 교내 배달 주소를 새로 추가
        - 사용자가 입력한 교내 배달 주소를 저장합니다.
        """)
    @PostMapping("/delivery/address/campus")
    ResponseEntity<UserDeliveryAddressResponse> addCampusDeliveryAddress(
        @RequestBody @Valid UserCampusDeliveryAddressRequest request,
        @Parameter(hidden = true) @Auth(permit = {GENERAL, STUDENT}) Integer userId
    );

    @Operation(summary = "교외 배달 주소 검증", description = """
        ### 사용자가 선택한 교외 배달 주소를 검증
        - 사용자가 입력한 주소가 배달 가능 주소인지 검증합니다.
        - 주소가 **충청남도 천안시 동남구 병천면** 이 아닌 경우 예외 발생
        """)
    @ApiResponseCodes({
        ApiResponseCode.OK,
        ApiResponseCode.INVALID_DELIVERY_AREA
    })
    @PostMapping("/delivery/address/off-campus/validate")
    ResponseEntity<Void> validateOffCampusDeliveryAddress(
        @RequestBody @Valid UserOffCampusDeliveryAddressValidateRequest request
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "배달 기사 요청 사항 목록 반환 성공",
            content = @Content(mediaType = "application/json", examples =
            @ExampleObject(
                value = """
                    {
                      "count": 5,
                      "contents": [
                        {
                          "content": "문 앞에 놔주세요 (벨 눌러주세요)"
                        },
                        {
                          "content": "문 앞에 놔주세요 (노크해주세요)"
                        },
                        {
                          "content": "문 앞에 놔주세요 (벨X, 노크 X)"
                        },
                        {
                          "content": "직접 받을게요"
                        },
                        {
                          "content": "전화주시면 마중 나갈게요"
                        }
                      ]
                    }
                        """)
            )
        )
    })
    @Operation(summary = "배달 기사 요청 사항 목록 조회", description = """
        ### 배달 기사 요청 사항 목록 조회
        - 배달 기사님 요청 사항을 반환 합니다.
        """)
    @GetMapping("/delivery/rider-message")
    ResponseEntity<RiderMessageResponse> getRiderMessages();
}
