package com.m2i.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponseDTO implements Serializable {
    private String teacherIdentifier;
    private String firstName;
    private String lastName;
    private String officeNumber;
    private String department;
    private String cni;
    private String email;
}
