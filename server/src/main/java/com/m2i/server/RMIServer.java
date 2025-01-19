package com.m2i.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import com.m2i.server.rmi.CoordinatorServiceImpl;
import com.m2i.shared.interfaces.AuthenticationService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
@DependsOn("SecurityUtilsBean")
public class RMIServer {
    @EJB
    private AuthenticationService authService;
    @PostConstruct
    public void init() {
        System.out.println(" = = = = = = = = = = = = =  = = = = = = = = =  = = = = = = =  =Starting RMI Server...");
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            CoordinatorServiceImpl coordService = new CoordinatorServiceImpl();
            registry.rebind("CoordinatorService", coordService);

            System.out.println(" ----------------------------- RMI Server is running...");
        } catch (Exception e) {
            System.err.println("RMI Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}