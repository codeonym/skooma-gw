package com.m2i.server.utils;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.EJB;
import com.m2i.server.dao.SessionDAO;

@Singleton
public class SessionCleanupService {
    @EJB
    private SessionDAO sessionDAO;

    @Schedule(hour = "*", minute = "*/30")  // Run every 30 minutes
    public void cleanupExpiredSessions() {
        sessionDAO.cleanupExpiredSessions();
    }
}