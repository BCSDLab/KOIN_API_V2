package in.koreatech.koin.domain.language.controller;

import org.springframework.http.ResponseEntity;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.language.service.ContentMessageCatalog;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.language.dto.LanguagesResponse;

@RestController
public class LanguageController implements LanguageApi {

    @Override
    @GetMapping("/languages/{language}/messages")
    public ResponseEntity<Map<String, String>> getMessages(@PathVariable String language) {
        return ResponseEntity.ok(ContentMessageCatalog.messages(SupportedLanguage.fromCode(language)));
    }

    @GetMapping("/languages")
    public ResponseEntity<LanguagesResponse> getLanguages() {
        return ResponseEntity.ok(LanguagesResponse.from());
    }
}
