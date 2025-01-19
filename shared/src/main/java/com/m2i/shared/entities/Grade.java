package com.m2i.shared.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime date;
    private int year;
    private Semester semester;
    private double value;
    @ManyToOne
    private Student student;
    @ManyToOne
    private Course course;
}
