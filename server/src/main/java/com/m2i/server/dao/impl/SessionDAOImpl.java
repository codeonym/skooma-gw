package com.m2i.server.dao.impl;

import com.m2i.server.dao.SessionDAO;
import com.m2i.shared.auth.UserSession;
import com.m2i.shared.entities.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.UUID;

@Stateless
public class SessionDAOImpl implements SessionDAO {
    @PersistenceContext
    private EntityManager em;

    private static final long SESSION_DURATION = 30 * 60 * 1000; // 30 minutes

    @Override
    public UserSession createSession(User user, String ipAddress) {
        // Clean up any existing sessions for this user
        em.createQuery("DELETE FROM UserSession s WHERE s.username = :username")
                .setParameter("username", user.getUsername())
                .executeUpdate();

        UserSession session = new UserSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUsername(user.getUsername());
        session.setRole(user.getRole().name());
        session.setUser(user);
        session.setActive(true);
        session.setIpAddress(ipAddress);

        long currentTime = System.currentTimeMillis();
        session.setCreationTime(currentTime);
        session.setLastAccessedTime(currentTime);
        session.setExpirationTime(currentTime + SESSION_DURATION);

        em.persist(session);
        return session;
    }
    @Override
    public UserSession findBySessionId(String sessionId) {
        try {
            UserSession session = em.find(UserSession.class, sessionId);
            if (session != null && session.isActive() && session.getExpirationTime() > System.currentTimeMillis()) {
                // Update last accessed time
                session.setLastAccessedTime(System.currentTimeMillis());
                em.merge(session);
                return session;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    public void invalidateSession(String sessionId) {
        UserSession session = em.find(UserSession.class, sessionId);
        if (session != null) {
            session.setActive(false);
            em.merge(session);
        }
    }
    @Override
    public void cleanupExpiredSessions() {
        em.createQuery("DELETE FROM UserSession s WHERE s.expirationTime < :currentTime OR s.active = false")
                .setParameter("currentTime", System.currentTimeMillis())
                .executeUpdate();
    }
}