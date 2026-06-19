package com.employeehub.employeehub.features.members.util;

import com.employeehub.employeehub.features.members.dto.JobTitleDtos.JobTitleRecordDto;
import com.employeehub.employeehub.features.members.entity.JobTitleRecord;

public class JobTitleUtils {

    private JobTitleUtils() {}

    public static JobTitleRecordDto toDto(JobTitleRecord r) {
        return new JobTitleRecordDto(r.getId(), r.getJobTitle(), r.getChangeType(), r.getEffectiveDate(), r.getNotes(), r.getCreatedAt());
    }
}
