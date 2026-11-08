package in.koreatech.koin.domain.language.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.language.dto.LanguagesResponse;

@RestController
public class LanguageController implements LanguageApi {

    @GetMapping("/languages")
    public ResponseEntity<LanguagesResponse> getLanguages() {
        return ResponseEntity.ok(LanguagesResponse.from());
    }
}
