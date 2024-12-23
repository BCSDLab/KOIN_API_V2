package in.koreatech.koin.domain.coop.dto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import in.koreatech.koin.global.exception.KoinIllegalStateException;

public class CompressFileResponseBuilder {

    public static ResponseEntity<Resource> buildCompressFileResponse(File zipFile) {
        InputStreamResource resource = null;
        try {
            resource = new InputStreamResource(new FileInputStream(zipFile));
        } catch (FileNotFoundException e) {
            throw new KoinIllegalStateException("존재하지 않는 압축 파일입니다. " + e.getMessage());
        }
        String encodedFileName = URLEncoder.encode(zipFile.getName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        // HTTP 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(zipFile.length())
            .body(resource);
    }
}
