package com.m2i.server.dao.impl;

import com.m2i.server.dao.UserDAO;
import com.m2i.shared.auth.UserCredentials;
import com.m2i.shared.entities.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class UserDAOImpl implements UserDAO {
    @PersistenceContext
    private EntityManager em;
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            em.persist(user);
            return user;
        } else {
            return em.merge(user);
        }
    }
    @Override
    public User findById(Long id) {
        return em.find(User.class, id);
    }
    @Override
    public User findByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    public User findByCredentials(UserCredentials credentials) {

        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username AND u.password = :password",
                            User.class)
                    .setParameter("username", credentials.getUsername())
                    .setParameter("password", credentials.getPassword())
                    .getSingleResult();
        } catch (Exception e) {
            System.out.println("User not found");
            System.out.println(e.getMessage());
            return null;
        }
    }
    @Override
    public void delete(Long id) {
        User user = findById(id);
        if (user != null) {
            em.remove(user);
        }
    }
}