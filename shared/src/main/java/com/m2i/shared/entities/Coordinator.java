package com.m2i.shared.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coordinators")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Coordinator extends User {
    @Column(unique = true)
    private String coordinatorId;
    @Column(unique = true)
    private String coordinatorIdentifier;
    private String department;
    private String officeNumber;
}