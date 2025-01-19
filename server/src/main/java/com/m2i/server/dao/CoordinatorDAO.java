package com.m2i.server.dao;

import com.m2i.shared.entities.Coordinator;
import jakarta.ejb.Remote;

import java.util.List;

@Remote
public interface CoordinatorDAO {

    public Coordinator save(Coordinator coordinator);

    public Coordinator findById(Long id);

    public List<Coordinator> findAll();

    public List<Coordinator> findByDepartment(String department);
    public void delete(Long id);
    public Coordinator findByUsername(String username);
}