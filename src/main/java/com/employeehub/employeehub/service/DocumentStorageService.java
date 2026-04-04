package com.employeehub.employeehub.service;

import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.dto.DocumentDtos.*;
import com.employeehub.employeehub.dto.DocumentDtos.DocumentDto;
import com.employeehub.employeehub.entity.CompanyMember;
import com.employeehub.employeehub.entity.Document;
import com.employeehub.employeehub.entity.Permission;
import com.employeehub.employeehub.exception.BadRequestException;
import com.employeehub.employeehub.exception.NotFoundException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.DocumentRepository;
import com.employeehub.employeehub.util.DocumentUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class DocumentStorageService {

    private final DocumentRepository documentRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final int presignExpireMinutes;
    private final long maxSizeBytes;
    private final List<String> allowedContentTypes;
    private final PermissionService permissionService;

    public DocumentStorageService(DocumentRepository documentRepository, CompanyMemberRepository companyMemberRepository, S3Client s3Client, S3Presigner s3Presigner, @Value("${aws.s3.bucket}") String bucketName, @Value("${aws.s3.presignExpireMinutes}") int presignExpireMinutes, @Value("${document.maxSizeBytes}") long maxSizeBytes, @Value("${document.allowedContentTypes}") List<String> allowedContentTypes, PermissionService permissionService) {
        this.documentRepository = documentRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.presignExpireMinutes = presignExpireMinutes;
        this.maxSizeBytes = maxSizeBytes;
        this.allowedContentTypes = allowedContentTypes;
        this.permissionService = permissionService;
    }

    // ── Admin methods ──

    @Transactional
    public DocumentDto upload(UUID companyId, UUID memberId, AppUserDetails principal, MultipartFile file, String fileName, LocalDate expiryDate) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.MANAGE_DOCUMENTS);

        CompanyMember member = companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return doUpload(companyId, member, file, fileName, expiryDate);
    }

    public DocumentDownloadDto download(UUID companyId, UUID memberId, UUID documentId, AppUserDetails principal) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.MANAGE_DOCUMENTS);

        return doDownload(memberId, documentId);
    }

    public Page<DocumentDto> list(UUID companyId, UUID memberId, AppUserDetails principal, Pageable pageable) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkPermission(caller, Permission.MANAGE_DOCUMENTS);

        companyMemberRepository.findByCompanyIdAndId(companyId, memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return documentRepository
                .findByCompanyMemberIdOrderByUploadedAtDesc(memberId, pageable)
                .map(DocumentUtils::toDto);
    }

    // ── Self-service methods ──

    @Transactional
    public DocumentDto uploadSelf(UUID companyId, AppUserDetails principal, MultipartFile file, String fileName, LocalDate expiryDate) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkSelfServiceAccess(caller);

        return doUpload(companyId, caller, file, fileName, expiryDate);
    }

    public DocumentDownloadDto downloadSelf(UUID companyId, UUID documentId, AppUserDetails principal) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkSelfServiceAccess(caller);

        return doDownload(caller.getId(), documentId);
    }

    public Page<DocumentDto> listSelf(UUID companyId, AppUserDetails principal, Pageable pageable) {

        CompanyMember caller = permissionService.getCallerOrThrow(principal, companyId);
        permissionService.checkSelfServiceAccess(caller);

        return documentRepository
                .findByCompanyMemberIdOrderByUploadedAtDesc(caller.getId(), pageable)
                .map(DocumentUtils::toDto);
    }

    // ── Shared logic ──

    private DocumentDto doUpload(UUID companyId, CompanyMember member, MultipartFile file, String fileName, LocalDate expiryDate) {

        if (file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new BadRequestException("File exceeds maximum allowed size of " + (maxSizeBytes / 1024 / 1024) + " MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new BadRequestException("File type not allowed. Allowed types: " + String.join(", ", allowedContentTypes));
        }

        String resolvedFileName = (fileName != null && !fileName.isBlank()) ? fileName.trim() : file.getOriginalFilename();

        if (resolvedFileName == null || resolvedFileName.isBlank()) {
            throw new BadRequestException("File name is required");
        }

        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Expiry date cannot be in the past");
        }

        Document newDocument = Document
                .builder()
                .companyMember(member)
                .contentType(contentType)
                .fileSize(file.getSize())
                .fileName(resolvedFileName)
                .s3Key("pending")
                .expiryDate(expiryDate)
                .build();

        newDocument = documentRepository.saveAndFlush(newDocument);

        String s3Key = String.format("uploads/%s/%s/%s/%s", companyId, member.getId(), newDocument.getId(), resolvedFileName);
        newDocument.setS3Key(s3Key);
        newDocument = documentRepository.saveAndFlush(newDocument);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file", e);
        }

        return DocumentUtils.toDto(newDocument);
    }

    private DocumentDownloadDto doDownload(UUID memberId, UUID documentId) {

        Document document = documentRepository.findByIdAndCompanyMemberId(documentId, memberId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(document.getS3Key())
                .responseContentDisposition("inline; filename=\"" + document.getFileName().replaceAll("[\"\\\\]", "_") + "\"")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignExpireMinutes))
                .getObjectRequest(getRequest)
                .build();

        String downloadUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        return new DocumentDownloadDto(document.getId(), document.getFileName(), downloadUrl);
    }
}
