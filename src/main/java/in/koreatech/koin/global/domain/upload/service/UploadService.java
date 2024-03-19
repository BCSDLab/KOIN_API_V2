package in.koreatech.koin.global.domain.upload.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.global.domain.upload.dto.UploadUrlRequest;
import in.koreatech.koin.global.domain.upload.dto.UploadUrlResponse;
import in.koreatech.koin.global.domain.upload.model.ImageUploadDomain;
import in.koreatech.koin.global.s3.S3Utils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UploadService {

    private final S3Utils s3Util;
    private final Clock clock;

    public UploadUrlResponse getPresignedUrl(ImageUploadDomain domain, UploadUrlRequest request) {
        var filePath = generateFilePath(domain.name(), request.fileName());
        return s3Util.getUploadUrl(filePath);
    }

    private String generateFilePath(String domainName, String fileNameExt) {
        var now = LocalDateTime.now(clock);
        StringJoiner uploadPrefix = new StringJoiner("/");
        String[] parts = fileNameExt.split("\\.");
        String fileExt = parts[parts.length - 1];
        String fileName = String.join("", Arrays.copyOf(parts, parts.length - 1));
        uploadPrefix.add("upload")
            .add(domainName)
            .add(String.valueOf(now.getYear()))
            .add(String.valueOf(now.getMonth().getValue()))
            .add(String.valueOf(now.getDayOfMonth()))
            .add(UUID.randomUUID().toString())
            .add(fileName);
        return uploadPrefix + "." + fileExt;
    }
}
