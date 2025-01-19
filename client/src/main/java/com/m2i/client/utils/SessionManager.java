// SessionManager.java
package com.m2i.client.utils;

import com.m2i.shared.auth.UserSession;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static SessionManager instance;
    private UserSession currentSession;
    private List<Runnable> expirationListeners = new ArrayList<>();

    private SessionManager() {
        startSessionMonitor();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentSession(UserSession session) {
        this.currentSession = session;
    }

    public UserSession getCurrentSession() {
        return currentSession;
    }

    public void clearSession() {
        currentSession = null;
    }

    public void addSessionExpirationListener(Runnable listener) {
        expirationListeners.add(listener);
    }

    private void startSessionMonitor() {
        Thread monitor = new Thread(() -> {
            while (true) {
                if (currentSession != null && currentSession.getExpirationTime() > System.currentTimeMillis()) {
                    notifySessionExpired();
                }
                try {
                    Thread.sleep(60000); // Check every minute
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }

    private void notifySessionExpired() {
        expirationListeners.forEach(Runnable::run);
    }
}