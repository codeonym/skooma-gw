package com.m2i.shared.interfaces;

import com.m2i.shared.auth.UserCredentials;
import com.m2i.shared.auth.UserSession;

import jakarta.ejb.Remote;


@Remote
public interface AuthenticationService {
    UserSession login(UserCredentials credentials);
    void logout(String sessionId);
    void updatePassword(String sessionId, String newPassword);
    boolean validateSession(String sessionId);
}