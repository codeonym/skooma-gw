package com.m2i.server.utils;

import jakarta.ejb.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

@Singleton
public class ServiceLocator {
    public static <T> T lookup(String jndiName) {
        try {
            Properties props = new Properties();
            props.put("java.naming.factory.initial", "org.wildfly.naming.client.WildFlyInitialContextFactory");
            props.put("java.naming.provider.url", "remote+http://localhost:8080");  // Adjust the URL as needed

            InitialContext context = new InitialContext(props);
            return (T) context.lookup(jndiName);
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup EJB: " + jndiName, e);
        }
    }
}