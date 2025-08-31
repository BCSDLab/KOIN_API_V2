package in.koreatech.koin.global.validation;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class FileTypeValidator implements ConstraintValidator<FileTypeValid, MultipartFile> {

    private Set<String> allowedExtensions;
    private boolean nullable;

    @Override
    public void initialize(FileTypeValid annotation) {
        this.allowedExtensions = Arrays.stream(annotation.extensions())
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        this.nullable = annotation.nullable();
    }

    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return nullable;
        }

        return allowedExtensions.isEmpty() || isValidExtension(file);
    }

    private boolean isValidExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }

        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) {
            return false;
        }

        String extension = fileName.substring(lastDot + 1).toLowerCase();
        return allowedExtensions.contains(extension);
    }
}
