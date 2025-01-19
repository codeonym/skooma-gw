package com.m2i.shared.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "teachers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Teacher extends User {
    @Column(unique = true)
    private String teacherId;
    @Column(unique = true)
    private String teacherIdentifier;
    private String department;
    private String officeNumber;
    private String specialization;
    @OneToMany(mappedBy = "teacher")
    private List<Course> courses;
}