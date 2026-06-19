package com.employeehub.employeehub.features.documents.util;

import com.employeehub.employeehub.features.documents.dto.DocumentDtos.DocumentDto;
import com.employeehub.employeehub.features.documents.entity.Document;

public class DocumentUtils {

    private DocumentUtils() {}

    public static DocumentDto toDto(Document d) {
        return new DocumentDto(
                d.getId(),
                d.getFileName(),
                d.getContentType(),
                d.getFileSize(),
                d.getExpiryDate(),
                d.getUploadedAt()
        );
    }
}
