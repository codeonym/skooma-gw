package com.m2i.server.ejb;


import com.m2i.shared.auth.UserSession;
import jakarta.ejb.EJB;
import com.m2i.shared.auth.UserCredentials;
import com.m2i.shared.interfaces.AuthenticationService;
import com.m2i.shared.entities.User;
import com.m2i.server.dao.UserDAO;
import com.m2i.server.dao.SessionDAO;
import jakarta.ejb.Stateless;

@Stateless
public class AuthenticationServiceBean implements AuthenticationService {
    @EJB
    private UserDAO userDAO;

    @EJB
    private SessionDAO sessionDAO;

    @Override
    public UserSession login(UserCredentials credentials){
        User user = userDAO.findByCredentials(credentials);
        if (user != null) {
            // Create new session
            UserSession persistentSession =
                    sessionDAO.createSession(user, "client-ip"); // In production, get actual IP

            // Convert to DTO for client
            UserSession sessionDTO = new UserSession();
            sessionDTO.setSessionId(persistentSession.getSessionId());
            sessionDTO.setUsername(persistentSession.getUsername());
            sessionDTO.setRole(persistentSession.getRole());
            sessionDTO.setExpirationTime(persistentSession.getExpirationTime());

            return sessionDTO;
        }
        return null;
    }

    @Override
    public void logout(String sessionId){
        sessionDAO.invalidateSession(sessionId);
    }

    @Override
    public void updatePassword(String sessionId, String newPassword) {
        UserSession session = sessionDAO.findBySessionId(sessionId);
        if (session != null) {
            User user = userDAO.findByUsername(session.getUsername());
            user.setPassword(newPassword);
            userDAO.save(user);
        }
    }

    @Override
    public boolean validateSession(String sessionId){
        return sessionDAO.findBySessionId(sessionId) != null;
    }
}