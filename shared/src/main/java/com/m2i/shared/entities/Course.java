package com.m2i.shared.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private byte[] content;
    @Enumerated(EnumType.STRING)
    private Semester semester;
    @ManyToOne
    private Teacher teacher;
    @ManyToMany
    @JoinTable(
            name = "courses_students",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Student> enrolledStudents;
    @OneToMany(mappedBy = "course")
    private List<Grade> grades;
}
