package com.shehia_management.api.shared.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * NEW: local-disk implementation of FileStorageService.
 *
 * Saves under uploads/{category}/ (same "uploads/" root the app already uses
 * for generated letter PDFs in LetterServiceImpl.reviewLetter) and returns a
 * "/uploads/{category}/{filename}" URL, served statically by the resource
 * handler registered in FileStorageConfig.
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    // Matches spring.servlet.multipart.max-file-size in application.properties.
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "heic", "heif", "pdf"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/heic", "image/heif",
            "application/pdf"
    );

    @Override
    public String store(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File is too large; maximum size is 10MB");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extensionOf(originalName);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Only JPG, PNG, WEBP, HEIC photos or PDF documents are allowed.");
        }

        // Camera captures / some browsers send a generic or missing content
        // type (e.g. application/octet-stream) — trust the extension check
        // above in that case rather than rejecting a genuine capture.
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()
                && !contentType.equalsIgnoreCase("application/octet-stream")
                && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Only JPG, PNG, WEBP, HEIC photos or PDF documents are allowed.");
        }

        try {
            Path dir = Path.of("uploads", category);
            Files.createDirectories(dir);

            String filename = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/" + category + "/" + filename;
            log.info("Stored uploaded file '{}' ({} bytes) as {}", originalName, file.getSize(), url);
            return url;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file: " + e.getMessage(), e);
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }
}
