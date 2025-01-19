package com.m2i.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradeResponseDTO implements Serializable {
    private Long id;
    private String course;
    private double grade;
    private String semester;
    private int year;
    private LocalDateTime date;
}
