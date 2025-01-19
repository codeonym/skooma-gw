package com.m2i.server.dao;

import jakarta.ejb.Remote;
import com.m2i.shared.entities.User;
import com.m2i.shared.auth.UserCredentials;

@Remote
public interface UserDAO {
    public User save(User user);

    public User findById(Long id);

    public User findByUsername(String username);

    public User findByCredentials(UserCredentials credentials);

    public void delete(Long id);
}