package com.employeehub.employeehub.util;

import com.employeehub.employeehub.dto.DocumentDtos.DocumentDto;
import com.employeehub.employeehub.entity.Document;

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
