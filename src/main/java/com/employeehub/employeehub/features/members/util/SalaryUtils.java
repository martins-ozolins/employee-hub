package com.employeehub.employeehub.features.members.util;

import com.employeehub.employeehub.features.members.dto.SalaryDtos.SalaryRecordDto;
import com.employeehub.employeehub.features.members.entity.SalaryRecord;

public class SalaryUtils {

    private SalaryUtils() {}

    public static SalaryRecordDto toDto(SalaryRecord r) {
        return new SalaryRecordDto(r.getId(), r.getAmount(), r.getCurrency(), r.getEffectiveDate(), r.getNotes(), r.getCreatedAt());
    }
}
