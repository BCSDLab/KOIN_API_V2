package in.koreatech.koin.integration.s3.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.integration.s3.dto.UploadFileResponse;
import in.koreatech.koin.integration.s3.dto.UploadFilesResponse;
import in.koreatech.koin.integration.s3.dto.UploadUrlRequest;
import in.koreatech.koin.integration.s3.dto.UploadUrlResponse;
import in.koreatech.koin.integration.s3.model.ImageUploadDomain;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.integration.s3.client.S3Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UploadService {

    private final S3Client s3Client;
    private final Clock clock;

    public UploadUrlResponse getPresignedUrl(ImageUploadDomain domain, UploadUrlRequest request) {
        var filePath = generateFilePath(domain.name(), request.fileName());
        return s3Client.getUploadUrl(filePath);
    }

    public UploadFileResponse uploadFile(ImageUploadDomain domain, MultipartFile multipartFile) {
        try {
            var filePath = generateFilePath(domain.name(), Objects.requireNonNull(multipartFile.getOriginalFilename()));
            return s3Client.uploadFile(filePath, multipartFile.getBytes());
        } catch (Exception e) {
            log.warn("파일 업로드중 문제가 발생했습니다. file: {} \n message: {}", multipartFile, e.getMessage());
            throw new KoinIllegalArgumentException("파일 업로드중 오류가 발생했습니다.");
        }
    }

    public UploadFilesResponse uploadFiles(ImageUploadDomain domain, List<MultipartFile> files) {
        var response = files.stream()
            .map(file -> uploadFile(domain, file))
            .map(UploadFileResponse::fileUrl)
            .toList();
        return new UploadFilesResponse(response);
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
