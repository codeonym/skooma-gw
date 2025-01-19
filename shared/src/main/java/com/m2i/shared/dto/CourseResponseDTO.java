package com.m2i.shared.dto;

import com.m2i.shared.entities.Semester;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponseDTO implements Serializable {
    private Long courseId;
    private String name;
    private Semester semester;
    private byte[] content;
    private String teacher;
}
