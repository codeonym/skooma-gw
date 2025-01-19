package com.m2i.shared.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
public class GradesReport implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Student student;
    private boolean isGenerated;
    private byte[] report;
    private LocalDate requestDate;
    private LocalDate generationDate;
    private Semester semester;
    private int year;
    private Status status;
}
