package com.m2i.server.dao.impl;

import com.m2i.server.dao.CoordinatorDAO;
import com.m2i.shared.entities.Coordinator;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class CoordinatorDAOImpl implements CoordinatorDAO {
    @PersistenceContext
    private EntityManager em;
    @Override
    public Coordinator save(Coordinator coordinator) {
        coordinator.setCoordinatorId("COORD-"+ coordinator.getCni() + "-" + System.currentTimeMillis());
        if (coordinator.getId() == null) {
            em.persist(coordinator);
            return coordinator;
        } else {
            return em.merge(coordinator);
        }
    }
    @Override
    public Coordinator findById(Long id) {
        return em.find(Coordinator.class, id);
    }
    @Override
    public List<Coordinator> findAll() {
        return em.createQuery("SELECT c FROM Coordinator c", Coordinator.class)
                .getResultList();
    }
    @Override
    public List<Coordinator> findByDepartment(String department) {
        return em.createQuery(
                        "SELECT c FROM Coordinator c WHERE c.department = :department",
                        Coordinator.class)
                .setParameter("department", department)
                .getResultList();
    }
    @Override
    public void delete(Long id) {
        Coordinator coordinator = findById(id);
        if (coordinator != null) {
            em.remove(coordinator);
        }
    }

    @Override
    public Coordinator findByUsername(String username) {
        return em.createQuery(
                        "SELECT c FROM Coordinator c WHERE c.username = :username",
                        Coordinator.class)
                .setParameter("username", username)
                .getSingleResult();
    }
}