package com.employeehub.employeehub.features.selfservice.controller;

import com.employeehub.employeehub.features.documents.dto.DocumentDtos.*;
import com.employeehub.employeehub.features.documents.service.DocumentStorageService;
import com.employeehub.employeehub.features.members.dto.CompanyMemberDtos.*;
import com.employeehub.employeehub.features.members.service.CompanyMemberService;
import com.employeehub.employeehub.security.model.AppUserDetails;
import com.employeehub.employeehub.shared.dto.ApiResponses.*;
import jakarta.validation.Valid;
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
@RequestMapping("/companies/{id}/me")
public class SelfServiceController {

    private final CompanyMemberService companyMemberService;
    private final DocumentStorageService documentStorageService;

    public SelfServiceController(CompanyMemberService companyMemberService, DocumentStorageService documentStorageService) {
        this.companyMemberService = companyMemberService;
        this.documentStorageService = documentStorageService;
    }

    @GetMapping
    public ResponseEntity<DataResponse<MemberSelfResponseDto>> getSelf(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails principal
    ) {
        MemberSelfResponseDto dto = companyMemberService.getSelf(id, principal);
        return ResponseEntity.ok(new DataResponse<>(dto));
    }

    @PutMapping
    public ResponseEntity<DataResponse<MemberSelfResponseDto>> updateSelf(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody MemberSelfUpdateDto dto
    ) {
        MemberSelfResponseDto updated = companyMemberService.updateSelf(id, principal, dto);
        return ResponseEntity.ok(new DataResponse<>(updated));
    }

    @GetMapping("/documents")
    public ResponseEntity<PagedDataResponse<DocumentDto>> listDocuments(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails principal,
            Pageable pageable
    ) {
        Page<DocumentDto> page = documentStorageService.listSelf(id, principal, pageable);

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

    @PostMapping("/documents")
    public ResponseEntity<MessageAndDataResponse<DocumentDto>> uploadDocument(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileName") String fileName,
            @RequestParam(value = "expiryDate", required = false) LocalDate expiryDate
    ) {
        DocumentDto document = documentStorageService.uploadSelf(id, principal, file, fileName, expiryDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageAndDataResponse<>("Document uploaded", document));
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<DataResponse<DocumentDownloadDto>> downloadDocument(
            @PathVariable UUID id,
            @PathVariable UUID documentId,
            @AuthenticationPrincipal AppUserDetails principal
    ) {
        DocumentDownloadDto download = documentStorageService.downloadSelf(id, documentId, principal);
        return ResponseEntity.ok(new DataResponse<>(download));
    }
}
