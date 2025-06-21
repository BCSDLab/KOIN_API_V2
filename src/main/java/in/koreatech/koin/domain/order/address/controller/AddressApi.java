package in.koreatech.koin.domain.order.address.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import in.koreatech.koin.domain.order.address.dto.AddressSearchRequest;
import in.koreatech.koin.domain.order.address.dto.AddressSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Common) Address: 주소", description = "주소 API")
public interface AddressApi {

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "주소 검색 성공",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "검색 결과 존재", value = """
                    {
                      "total_count": "203",
                      "current_page": 1,
                      "count_per_page": 2,
                      "addresses": [
                        {
                          "road_addr": "충청남도 천안시 동남구 병천면 충절로 1506-4",
                          "jibun_addr": "충청남도 천안시 동남구 병천면 가전리 278-1",
                          "eng_addr": "1506-4 Chungjeol-ro, Byeongcheon-myeon, Dongnam-gu, Cheonan-si, Chungcheongnam-do",
                          "zip_no": "31253"
                        },
                        {
                          "road_addr": "충청남도 천안시 동남구 병천면 충절로 1506-6",
                          "jibun_addr": "충청남도 천안시 동남구 병천면 가전리 278-1",
                          "eng_addr": "1506-6 Chungjeol-ro, Byeongcheon-myeon, Dongnam-gu, Cheonan-si, Chungcheongnam-do",
                          "zip_no": "31253"
                        }
                      ]
                    }
                    """),
                @ExampleObject(name = "검색 결과 없음", value = """
                    {
                      "total_count": "0",
                      "current_page": 1,
                      "count_per_page": 10,
                      "addresses": []
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "검색어 글자 수 부족", value = """
                    {
                      "code": "KEYWORD_TOO_SHORT",
                      "message": "검색어는 두글자 이상 입력되어야 합니다.",
                      "errorTraceId": "a3e73b85-8c84-41f1-9f24-e6a4ae38aea3"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "외부 API 연동 실패", summary = "외부 API 시스템 오류", value = """
                    {
                      "code": "EXTERNAL_API_ERROR",
                      "message": "주소 정보를 가져오는 중 오류가 발생했습니다.",
                      "errorTraceId": "a3e73b85-8c84-41f1-9f24-e6a4ae38aea3"
                    }
                    """)
            })
        )
    })
    @Operation(summary = "주소 검색", description = """
        ### 키워드를 사용하여 주소를 검색합니다.
        - 행정안전부 주소기반산업지원서비스 API를 호출하여 결과를 반환합니다.
                
        ### 요청 파라미터
        - **keyword**: 검색할 주소 (**필수**, 2자 이상, 한글 40자 이하, 숫자만 검색 불가, 시도명으로는 검색 불가, 특수문자(%,=,>,<,[,]) 불가)
        - **current_page**: 현재 페이지 (1 이상)
        - **count_per_page**: 페이지당 항목 수 (1 이상)
        """)
    @GetMapping("/api/addresses/search")
    ResponseEntity<AddressSearchResponse> searchAddress(
        @ParameterObject @Valid AddressSearchRequest request
    );
}
