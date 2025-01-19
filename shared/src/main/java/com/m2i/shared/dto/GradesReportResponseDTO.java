package com.m2i.shared.dto;

import com.m2i.shared.entities.Semester;
import com.m2i.shared.entities.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradesReportResponseDTO implements Serializable {
    private Long id;
    private String studentId;
    private boolean isGenerated;
    private byte[] report;
    private LocalDate requestDate;
    private LocalDate generationDate;
    private Status status;
    private Semester semester;
}
