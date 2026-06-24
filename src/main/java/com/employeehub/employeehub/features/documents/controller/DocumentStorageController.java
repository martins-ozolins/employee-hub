package com.employeehub.employeehub.features.documents.controller;

import com.employeehub.employeehub.features.documents.dto.DocumentDtos.*;
import com.employeehub.employeehub.features.documents.service.DocumentStorageService;
import com.employeehub.employeehub.security.model.AuthenticatedUser;
import com.employeehub.employeehub.shared.dto.ApiResponses.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/members/{memberId}/documents")
public class DocumentStorageController {

    private final DocumentStorageService documentStorageService;

    public DocumentStorageController(DocumentStorageService documentStorageService) {
        this.documentStorageService = documentStorageService;
    }

    @PostMapping
    public ResponseEntity<MessageAndDataResponse<DocumentDto>> uploadDocument(
            @PathVariable UUID companyId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileName") String fileName,
            @RequestParam(value = "expiryDate", required = false) LocalDate expiryDate
    ) {

        DocumentDto document = documentStorageService.upload(companyId, memberId, principal, file, fileName, expiryDate);

        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageAndDataResponse<>("Document uploaded", document));
    }

    @DeleteMapping("/{docId}")
    public ResponseEntity<MessageResponse> deleteDocument(
            @PathVariable UUID companyId,
            @PathVariable UUID memberId,
            @PathVariable UUID docId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        documentStorageService.delete(companyId, memberId, docId, principal);

        return ResponseEntity.ok(new MessageResponse("Document deleted successfully"));

    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<DataResponse<DocumentDownloadDto>> downloadDocument(
            @PathVariable UUID companyId,
            @PathVariable UUID memberId,
            @PathVariable UUID documentId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        DocumentDownloadDto download = documentStorageService.download(companyId, memberId, documentId, principal);

        return ResponseEntity.ok(new DataResponse<>(download));
    }

    @GetMapping
    public ResponseEntity<PagedDataResponse<DocumentDto>> listDocuments(
            @PathVariable UUID companyId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            Pageable pageable
    ) {

        Page<DocumentDto> page = documentStorageService.list(companyId, memberId, principal, pageable);

        return ResponseEntity.ok(new PagedDataResponse<>(
                page.getContent(),
                new PageMeta(
                        page.getNumber() + 1,
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.hasNext(),
                        page.hasPrevious()
                )
        ));
    }
}
