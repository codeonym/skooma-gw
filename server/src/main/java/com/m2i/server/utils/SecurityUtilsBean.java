package com.m2i.server.utils;

import com.m2i.shared.entities.UserRole;
import com.m2i.shared.auth.UserSession;
import com.m2i.shared.utils.SecurityUtils;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;
import com.m2i.server.dao.SessionDAO;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class SecurityUtilsBean implements SecurityUtils {
    @EJB
    private SessionDAO sessionDAO;

    @Override
    public void checkAuthorization(String sessionId, UserRole... allowedRoles) {
        UserSession session = sessionDAO.findBySessionId(sessionId);
        if (session == null) {
            throw new SecurityException("Invalid or expired session");
        }
        log.info("Checking authorization for user: " + session.getUsername());
        boolean authorized = false;
        for (UserRole role : allowedRoles) {
            if (role.name().equals(session.getRole())) {
                authorized = true;
                break;
            }
        }

        if (!authorized) {
            throw new SecurityException("Unauthorized access");
        }
    }
}