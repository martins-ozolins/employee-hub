package com.employeehub.employeehub.util;

import com.employeehub.employeehub.dto.JobTitleDtos.JobTitleRecordDto;
import com.employeehub.employeehub.entity.JobTitleRecord;

public class JobTitleUtils {

    private JobTitleUtils() {}

    public static JobTitleRecordDto toDto(JobTitleRecord r) {
        return new JobTitleRecordDto(r.getId(), r.getJobTitle(), r.getChangeType(), r.getEffectiveDate(), r.getNotes(), r.getCreatedAt());
    }
}