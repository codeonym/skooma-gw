package com.m2i.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequestDTO implements Serializable {
    private String studentId;
    private String firstName;
    private String lastName;
    private String cne;
    private String email;
    private String cni;
    private String apogee;
}
