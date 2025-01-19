package com.m2i.shared.utils;

import com.m2i.shared.entities.UserRole;
import jakarta.ejb.Remote;

@Remote
public interface SecurityUtils {
    public void checkAuthorization(String sessionId, UserRole... allowedRoles);
}
