package com.m2i.shared.dto;

import com.m2i.shared.entities.Semester;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradesReportRequestDTO implements Serializable {
    private String studentId;
    private int year;
    private Semester semester;
}
