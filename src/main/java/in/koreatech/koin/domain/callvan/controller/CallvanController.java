package in.koreatech.koin.domain.callvan.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanPostCreateResponse;
import in.koreatech.koin.domain.callvan.service.CallvanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CallvanController implements CallvanApi {

    private final CallvanService callvanService;

    @PostMapping
    public ResponseEntity<CallvanPostCreateResponse> createCallvanPost(
        @RequestBody CallvanPostCreateRequest request,
        Integer userId
    ) {
        CallvanPostCreateResponse response = callvanService.createCallvanPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
