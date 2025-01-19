package com.m2i.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDTO implements Serializable {
    private Long courseId;
    private byte[] content;
    private Long teacherId;
}
