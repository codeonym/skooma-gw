package com.m2i.client.utils;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import com.m2i.shared.interfaces.*;

import javax.naming.Context;
import javax.naming.InitialContext;

public class ServiceLocator {
    private static ServiceLocator instance;
    private Registry rmiRegistry;
    private Context ejbContext;

    private ServiceLocator() {
        try {
            // Initialize RMI registry
            rmiRegistry = LocateRegistry.getRegistry("localhost", 1099);

            // Initialize EJB context
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
            props.put(Context.PROVIDER_URL, "remote+http://localhost:8080"); // Adjust host and port as needed
            props.put("wildfly.naming.client.ejb.context", true);
            ejbContext = new InitialContext(props);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ServiceLocator", e);
        }
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    public AuthenticationService getAuthService() {
        try {
            return (AuthenticationService) ejbContext.lookup("ejb:/server-1.0-SNAPSHOT/AuthenticationServiceBean!com.m2i.shared.interfaces.AuthenticationService");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get AuthenticationService", e);
        }
    }

    public CoordinatorService getCoordinatorService() {
        try {
            return (CoordinatorService) rmiRegistry.lookup("CoordinatorService");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get CoordinatorService", e);
        }
    }

    public TeacherService getTeacherService() {
        try {
            return (TeacherService) ejbContext.lookup("ejb:/server-1.0-SNAPSHOT/TeacherServiceBean!com.m2i.shared.interfaces.TeacherService");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get TeacherService", e);
        }
    }

    public StudentService getStudentService() {
        try {
            return (StudentService) ejbContext.lookup("ejb:/server-1.0-SNAPSHOT/StudentServiceBean!com.m2i.shared.interfaces.StudentService");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get StudentService", e);
        }
    }
}