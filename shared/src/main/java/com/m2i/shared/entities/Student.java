package com.m2i.shared.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Student extends User {
    @Column(unique = true)
    private String studentId;
    private String cne;
    private String apogee;
    @ManyToMany
    @JoinTable(
            name = "courses_students",  // match the name in Course entity
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> enrolledCourses;
    @OneToMany(mappedBy = "student")
    private List<Grade> grades;
    @OneToMany(mappedBy = "student")
    private List<GradesReport> reports;
}