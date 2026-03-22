package com.employeehub.employeehub.util;

import com.employeehub.employeehub.dto.SalaryDtos.SalaryRecordDto;
import com.employeehub.employeehub.entity.SalaryRecord;

public class SalaryUtils {

    private SalaryUtils() {}

    public static SalaryRecordDto toDto(SalaryRecord r) {
        return new SalaryRecordDto(r.getId(), r.getAmount(), r.getCurrency(), r.getEffectiveDate(), r.getNotes(), r.getCreatedAt());
    }
}