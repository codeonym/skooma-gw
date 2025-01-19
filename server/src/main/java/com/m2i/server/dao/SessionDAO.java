package com.m2i.server.dao;

import com.m2i.shared.auth.UserSession;
import jakarta.ejb.Remote;
import com.m2i.shared.entities.User;

@Remote
public interface SessionDAO {
    public UserSession createSession(User user, String ipAddress);

    public UserSession findBySessionId(String sessionId);
    public void invalidateSession(String sessionId);

    public void cleanupExpiredSessions();
}