package com.shehia_management.api.shared.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * NEW: handles resident-uploaded photos and documents.
 *
 * Introduced so residents can attach a real file (or a photo taken directly
 * with their camera) for supportingDocUrl, photoUrl, idDocumentUrl and
 * proofOfResidenceUrl instead of typing a URL themselves. The controller
 * validates and stores the incoming MultipartFile here and gets back a URL
 * to put on the existing *Url fields — the DTOs, entities and admin-side
 * "Open document" links are all unchanged.
 */
public interface FileStorageService {

    /**
     * Validates (type + size) and saves an uploaded file under
     * uploads/{category}/, mirroring the existing generated-letter storage
     * convention already used across the capability services.
     *
     * @param file     the uploaded photo or document
     * @param category logical sub-folder, e.g. "id-documents",
     *                 "proof-of-residence", "supporting-docs", "issue-photos"
     * @return a public URL (e.g. "/uploads/issue-photos/&lt;uuid&gt;.jpg") to store
     *         on the entity/DTO in place of a user-supplied URL
     */
    String store(MultipartFile file, String category);
}
